import copy
import datetime as dt
import errno
import importlib
import inspect
import json
import logging
import logging.config
import os
import socket
import string
import threading
import uuid

class SydxException(Exception):
    pass

class Connection(object):
    def __init__(self, host, pid, local_port, connection_datetime, client):
        self.__host = host
        self.__pid = pid
        self.__local_port = local_port
        self.__connection_datetime = connection_datetime
        self.__client = client

    @property
    def host(self):
        return self.__host

    @property
    def pid(self):
        return self.__pid

    @property
    def local_port(self):
        return self.__local_port

    @property
    def connection_datetime(self):
        return self.__connection_datetime

    @property
    def client(self):
        return self.__client

    @client.setter
    def client(self, client):
        self.__client = client

class Connections(object):
    def __init__(self):
        self.__lock = threading.Lock()
        self.__connections = {}

    def add(self, handle, connection):
        with self.__lock:
            self.__connections[handle] = connection

    def send_to_all(self, request):
        with self.__lock:
            for handle, connection in self.__connections.items():
                if connection.client is None:
                    connection.client = _connect(connection.host, connection.local_port)
                connection.client.send_request(request)

class Storage(object):
    def __init__(self):
        self.__lock = threading.Lock()
        self.__dict = {}

    def __contains__(self, name):
        logger = logging.getLogger()
        logger.debug('Acquiring storage lock')
        with self.__lock:
            result = name in self.__dict
            logger.debug('Releasing storage lock')
        return result

    def put(self, name, value):
        logger = logging.getLogger()
        logger.debug('Acquiring storage lock')
        with self.__lock:
            self.__dict[name] = copy.copy(value)
            logger.debug('Releasing storage lock')

    def get(self, name):
        value = None
        logger = logging.getLogger()
        logger.debug('Acquiring storage lock')
        self.__lock.acquire()
        if name in self.__dict:
            value = copy.copy(self.__dict[name])
        logger.debug('Releasing storage lock')
        self.__lock.release()
        return value

    def get_all(self):
        values = {}
        logger = logging.getLogger()
        logger.debug('Acquiring storage lock')
        self.__lock.acquire()
        for name, value in self.__dict.items():
            values[name] = copy.copy(value)
        logger.debug('Releasing storage lock')
        self.__lock.release()
        return values

    def put_all(self, values):
        logger = logging.getLogger()
        logger.debug('Acquiring storage lock')
        self.__lock.acquire()
        for name, value in values.items():
            self.__dict[name] = copy.copy(value)
        logger.debug('Releasing storage lock')
        self.__lock.release()

class Interpreter(object):
    def __init__(self, storage, connections):
        self.__storage = storage
        self.__connections = connections

    def interpret(self, request_json):
        request_obj = json.loads(request_json)
        if request_obj['request_type'] == 'HANDSHAKE_REQUEST':
            handle = str(uuid.uuid1())
            host = request_obj['host']
            pid = request_obj['pid']
            local_port = request_obj['local_port']
            self.__connections.add(handle, Connection(host, pid, local_port, dt.datetime.utcnow(), client=None))
            response_obj = {
                'response_type': 'HANDSHAKE_RESPONSE',
                'connection_handle': handle
            }
        elif request_obj['request_type'] == 'SYNC_STORAGE_REQUEST':
            their_storage_snapshot = request_obj['storage_snapshot']
            self.__storage.put_all(their_storage_snapshot)
            our_storage_snapshot = self.__storage.get_all()
            response_obj = {
                'response_type': 'SYNC_STORAGE_RESPONSE',
                'storage_snapshot': our_storage_snapshot
            }
        elif request_obj['request_type'] == 'PUT_REQUEST':
            name = request_obj['name']
            value = request_obj['value']
            self.__storage.put(name, value)
            response_obj = {
                'response_type': 'PUT_RESPONSE',
                'result': 'SUCCESS'
            }
        else: raise SydxException('Unfamiliar request_type')
        response_json = json.dumps(response_obj)
        return response_json

class Server(object):
    def __init__(self, host, port, interpreter):
        self.__host = host
        self.__port = port
        self.__interpreter = interpreter
        self.__socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.__socket.bind((self.__host, self.__port))
        
    def listen(self):
        logger = logging.getLogger()
        self.__socket.listen(5)
        while threading.main_thread().is_alive():
            logger.info('Waiting for a client to connect to the socket')
            client, address = self.__socket.accept()
            client.settimeout(60)
            logger.info('Client connected. Kicking off a new thread to service this client')
            threading.Thread(target=self.listenToClient, args=(client, address)).start()
        self.__socket.close()
    
    def receive_line(self, client):
        size = 1024
        message = ''
        while True:
            chunk = client.recv(size)
            if not chunk: break
            message += chunk.decode('utf-8')
            if message.find('\n') != -1: break
        return message
            
    def listenToClient(self, client, address):
        request = self.receive_line(client)
        request = request.rstrip()
        response = self.__interpreter.interpret(request)
        response = response.rstrip() + '\n'
        client.send(response.encode())

class Client(object):
    def __init__(self, host, port, local_port, storage):
        self.__host = host
        self.__port = port
        self.__local_port = local_port
        self.__storage = storage
        self.__handle = None

    @property
    def handle(self):
        return self.__handle

    def receive_line(self, client):
        size = 1024
        message = ''
        while True:
            chunk = client.recv(size)
            if not chunk: break
            message += chunk.decode('utf-8')
            if message.find('\n') != -1: break
        return message

    def send_request(self, request):
        request_json = json.dumps(request)
        logger = logging.getLogger()
        self.__socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        logger.debug('Connecting on socket %s' % str(self.__socket))
        self.__socket.connect((self.__host, self.__port))
        logger.debug('Sending request: %s' % request_json)
        self.__socket.send((request_json + '\n').encode())
        logger.debug('Receiving response')
        response_json = self.receive_line(self.__socket)
        logger.debug('Received response: %s' % response_json)
        response = json.loads(response_json)
        self.__socket.close()
        return response

    def connect(self):
        request = {
            'request_type': 'HANDSHAKE_REQUEST',
            'host': socket.gethostname(),
            'pid': os.getpid(),
            'local_port': self.__local_port
        }
        response = self.send_request(request)
        self.__handle = response['connection_handle']
        values = self.__storage.get_all()
        request = {
            'request_type': 'SYNC_STORAGE_REQUEST',
            'storage_snapshot': values
        }
        response = self.send_request(request)
        their_storage_snapshot = response['storage_snapshot']
        self.__storage.put_all(their_storage_snapshot)

__version__ = '1.0.0'

def __init_logging():
    module_dir = os.path.dirname(os.path.abspath(__file__))
    logging_config_file_name = 'sydx-logging.cfg'
    default_config_file_path = os.path.join(module_dir, logging_config_file_name)
    config_file_path = os.getenv('SYDX_PYTHON_LOGGING_CONFIG', default_config_file_path)
    if not os.path.exists(config_file_path):
        config_file_path = os.path.join(module_dir, '..', '..', 'config', logging_config_file_name)
    if os.path.exists(config_file_path):
        logging.config.fileConfig(config_file_path)
    else:
        logging.basicConfig()

def __load_converters():
    module_names = os.getenv('SYDX_PYTHON_CONVERTER_MODULES', 'converters').split(',')
    all_from_json_object_converters, all_to_json_object_converters = [], []
    for module_name in module_names:
        module = importlib.import_module(module_name)
        from_json_object_converters = [o[1] for o in inspect.getmembers(module) if inspect.isfunction(o[1]) and o[0].endswith('_from_json_object')]
        to_json_object_converters = [o[1] for o in inspect.getmembers(module) if inspect.isfunction(o[1]) and o[0].endswith('_to_json_object')]
        all_from_json_object_converters.extend(from_json_object_converters)
        all_to_json_object_converters.extend(to_json_object_converters)
    return all_from_json_object_converters, all_to_json_object_converters

__init_logging()

logging.getLogger().info('Initialising Sydx version %s' % __version__)

__from_json_object_converters, __to_json_object_converters = __load_converters()
logging.getLogger().info('Loaded %d deserialisers and %d serialisers' % (len(__from_json_object_converters), len(__to_json_object_converters)))

__global_lock = threading.Lock()

__server_port = None

__storage = Storage()

__connections = Connections()

def version():
    return __version__

def port(port):
    global __server_port
    if __server_port is not None:
        raise SydxException('Port already open')
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    interpreter = Interpreter(__storage, __connections)
    server = Server('', port, interpreter)
    threading.Thread(target=server.listen, args=()).start()
    __server_port = port

def _connect(host, port):
    client = Client(host, port, __server_port, __storage)
    client.connect()
    return client

def connect(host, port):
    if __server_port is None:
        raise SydxException('Must open port before connecting')
    return _connect(host, port).handle

def server_port():
    return __server_port

def connections():
    pass

def __serialise(value):
    global __to_json_object_converters
    logger = logging.getLogger()
    json_object = None
    success = False
    for converter in __to_json_object_converters:
        logger.debug('Trying converter %s' % converter)
        try:
            json_object = converter(value)
        except Exception as e:
            logger.debug('The converter has failed')
            logger.debug(e)
            continue
        success = True
        break
    if not success:
        raise SydxException('There is no converter that can serialise the object')
    return json_object

def put(name, value):
    logger = logging.getLogger()
    logger.debug("Attempting to serialise the object named '%s'" % name)
    json_object = __serialise(value)
    logger.debug('Serialisation result: %s' % str(json_object))
    request = {
            'request_type': 'PUT_REQUEST',
            'name': name,
            'value': json_object
        }
    __connections.send_to_all(request)
    __storage.put(name, json_object)

def __deserialise(json_object):
    global __from_json_object_converters
    logger = logging.getLogger()
    obj = None
    success = False
    for converter in __from_json_object_converters:
        logger.debug('Trying converter %s' % converter)
        try:
            obj = converter(json_object)
        except Exception as e:
            logger.debug('The converter has failed')
            logger.debug(e)
            continue
        success = True
        break
    if not success:
        raise SydxException('There is no converter that can deserialise the object')
    return obj

def get(name):
    logger = logging.getLogger()
    json_object = __storage.get(name)
    logger.debug("Attempting to deserialise the object named '%s'" % name)
    obj = __deserialise(json_object)
    logger.debug('Deserialisation result: %s' % str(obj))
    return obj

def describe(obj):
    pass

def show(obj):
    pass

def represent(obj):
    pass

def evaluate(handle, code):
    pass

def save(file_path):
    pass

def load(file_path):
    pass
