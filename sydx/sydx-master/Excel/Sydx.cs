using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using ExcelDna.Integration;
using System.IO;
using Newtonsoft.Json.Bson;
using MongoDB.Bson;
using MongoDB.Driver;
using Amazon.Runtime.Internal;
using MongoDB.Bson.IO;
using MongoDB.Bson.Serialization;
using System.Collections;
using Microsoft.Win32.SafeHandles;
using System.Diagnostics;
using System.Linq;
using ExcelDna.Integration.Extensibility;
using System.Runtime.CompilerServices;

namespace Sydx
{
    public class ServerSydx
    {
        public class StateObject
        {
            // Client socket
            public Socket workSocket = null;
            // Size of receive buffer
            public const int BufferSize = 1024;
            // Receive buffer
            public byte[] buffer = new byte[BufferSize];
            // Received data string
            public StringBuilder sb = new StringBuilder();
        }

        private int port;

        private Storage storage;

        private ManualResetEvent allDone = new ManualResetEvent(false);

        public ServerSydx(int port, Storage storage)
        {
            this.port = port;
            this.storage = storage;
        }

        public void Run()
        {
            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            EndPoint localEndPoint = new IPEndPoint(IPAddress.Any, this.port);
            socket.Bind(localEndPoint);
            socket.Listen(100);
            while (true)
            {
                // Set the event to nonsignaled state
                allDone.Reset();

                socket.BeginAccept(new AsyncCallback(AcceptCallback), socket);

                // Wait until a connection is made before continuing
                allDone.WaitOne();
            }
        }

        public void AcceptCallback(IAsyncResult ar)
        {
            // Signal the thread to continue
            allDone.Set();

            Socket socket = (Socket)ar.AsyncState;
            Socket handler = socket.EndAccept(ar);

            this.storage.Put("connected", 357.0);

            StateObject state = new StateObject();
            state.workSocket = handler;
            handler.BeginReceive(state.buffer, 0, StateObject.BufferSize, 0,
                new AsyncCallback(ReadCallback), state);
        }

        public void ReadCallback(IAsyncResult ar)
        {
            String content = String.Empty;

            StateObject state = (StateObject)ar.AsyncState;
            Socket handler = state.workSocket;

            int bytesRead = handler.EndReceive(ar);

            if (bytesRead > 0)
            {
                // There might be more data, so store the data received so far
                state.sb.Append(Encoding.ASCII.GetString(state.buffer, 0, bytesRead));

                // Check for end-of-file tag. If it is not there, read more data
                content = state.sb.ToString();

                if (content.IndexOf("<EOF>") > -1)
                {
                    // All the data has been read from the client
                    this.storage.Put("message", content);

                    Send(handler, "Thank you.");
                }
                else
                {
                    // Not all data received. Get more
                    handler.BeginReceive(state.buffer, 0, StateObject.BufferSize, 0,
                        new AsyncCallback(ReadCallback), state);
                }
            }
        }

        private void Send(Socket handler, String data)
        {
            byte[] byteData = Encoding.ASCII.GetBytes(data);

            handler.BeginSend(byteData, 0, byteData.Length, 0, new AsyncCallback(SendCallback), handler);
        }

        private void SendCallback(IAsyncResult ar)
        {
            try
            {
                // Retrieve the socket from the state object
                Socket handler = (Socket)ar.AsyncState;

                // Complete sending the data to the remote device
                int bytesSent = handler.EndSend(ar);

                handler.Shutdown(SocketShutdown.Both);
                handler.Close();
            }
            catch (Exception e)
            {
                // TODO Log exception
            }
        }
    }

    public class Server
    {
        private String host;
        private int port;
        private Socket serverSocket;
        private Boolean mainThreadIsRunning;
        private Storage storage;
        private Connections connections;

        public Server(String host, int port, Storage storage, Connections connections)
        {
            this.host = host;
            this.port = port;
            this.storage = storage;
            this.connections = connections;
            this.mainThreadIsRunning = true;
        }

        private void listen()
        {
            TcpListener server = new TcpListener(IPAddress.Any, port);
            server.Start();

            Console.WriteLine("Listening on port: " + port);
            Console.WriteLine("Waiting for client to connect");

            while (mainThreadIsRunning)
            {
                TcpClient client = server.AcceptTcpClient();
                Console.WriteLine("Accepted client: " + client.Client.RemoteEndPoint);
                ListenToClient(client);
            }

            server.Stop();
        }

        public void stop()
        {
            this.mainThreadIsRunning = false;
        }
        
        private void ListenToClient(TcpClient client)
        {
            NetworkStream stream = client.GetStream();

            using (var reader = new BsonBinaryReader(stream))
            {
                int receivedLength = BsonSerializer.Deserialize<int>(reader);
                BsonDocument receivedRequest = BsonSerializer.Deserialize<BsonDocument>(reader);
                Console.WriteLine("Received from client: " + receivedRequest.ToJson());

                BsonDocument response = new Request(receivedRequest).GetResponse(storage, connections);
                using (var writer = new BsonBinaryWriter(stream))
                {
                    BsonSerializer.Serialize(writer, response.ToBson().Length);
                    Console.WriteLine("Sending response to client: " + response.ToJson());
                    BsonSerializer.Serialize(writer, response);
                }
            }
        }

        public void run()
        {
            this.listen();
        }
    }

    public class Client
    {
        private string host;
        private int port;
        private int localPort;
        private Storage storage;
        private TcpClient socket;
        private String handle;
        private int pid;

        public Client(string host, int port, int localPort, Storage storage)
        {
            this.host = host;
            this.port = port;
            this.localPort = localPort;
            this.storage = storage;
            this.handle = null;
        }

        public string Handle()
        {
            return this.handle;
        }

        public string Host()
        {
            return this.host;
        }

        public int Pid()
        {
            return this.pid;
        }

        public BsonDocument SendRequest(BsonDocument request)
        {
            BsonDocument receivedResponse;
            this.socket = new TcpClient(host, port);
            NetworkStream stream = socket.GetStream();

            using (var writer = new BsonBinaryWriter(stream))
            {
                Console.WriteLine("Sending request: " + request.ToJson());
                BsonSerializer.Serialize(writer, request.ToBson().Length);
                BsonSerializer.Serialize(writer, request);

                using (var reader = new BsonBinaryReader(stream))
                {
                    int receivedLength = BsonSerializer.Deserialize<int>(reader);
                    receivedResponse = BsonSerializer.Deserialize<BsonDocument>(reader);
                    Console.WriteLine("Received response: " + receivedResponse.ToJson());
                }
            }

            return receivedResponse;
        }

        public void Connect()
        {
            this.pid = Process.GetCurrentProcess().Id;
            BsonDocument requestHandshake = new BsonDocument(new BsonElement("request_type", "HANDSHAKE_REQUEST")).
                                                         Add(new BsonElement("host", Dns.GetHostName())).
                                                         Add(new BsonElement("pid", this.pid)).
                                                         Add(new BsonElement("local_port", this.localPort));
            BsonDocument responseHandshake = SendRequest(requestHandshake);
            this.handle = responseHandshake.GetValue("connection_handle").AsString;

            Dictionary<string, BsonDocument> serializedDict = storage.getAllSerialized();
            BsonDocument requestSynStorage = new BsonDocument(new BsonElement("request_type", "SYNC_STORAGE_REQUEST")).
                                                        Add(new BsonElement("storage_snapshot", serializedDict.ToBsonDocument()));

            BsonDocument responseSyncStorage = SendRequest(requestSynStorage);

            BsonDocument theirStorageSnapshot = responseSyncStorage.GetValue("storage_snapshot").AsBsonDocument;
            Dictionary<string, BsonDocument> dictSerialized = new Dictionary<string, BsonDocument>();

            foreach (BsonElement elem in theirStorageSnapshot)
            {
                dictSerialized.Add(elem.Name, elem.Value.AsBsonDocument);
            }

            storage.PutAllSerialized(dictSerialized);
        }

    }

    public class Connections
    {
        private Dictionary<string, Connection> connections;

        public Connections()
        {
            this.connections = new Dictionary<string, Connection>();
        }

        public void Add(string handle, Connection connection)
        {
            if (connections.ContainsKey(handle))
            {
                connections[handle] = connection;
            } else
            {
                connections.Add(handle, connection);
            }
        }

        public void SendToAll(BsonDocument request, Sydx sydx)
        {
            foreach (KeyValuePair<string, Connection> entry in connections)
            {
                Connection connection = entry.Value;

                if (connection.Client() is null)
                {
                    connection.setClient(sydx._connect(connection.Host(), connection.LocalPort()));
                }
                BsonDocument response = connection.Client().SendRequest(request);
                Console.WriteLine(response.AsString);
            }
        }
    }

    public class Connection
    {
        private String host;
        private int pid;
        private int localPort;
        private string dateTime;
        private Client client;

        public Connection(String host, int pid, int localPort, string dateTime, Client client)
        {
            this.host = host;
            this.pid = pid;
            this.localPort = localPort;
            this.dateTime = dateTime;
            this.client = client;
        }

        public String Host()
        {
            return this.host;
        }

        public int Pid()
        {
            return this.pid;
        }

        public int LocalPort()
        {
            return this.localPort;
        }

        public string DateTime()
        {
            return this.dateTime;
        }

        public Client Client()
        {
            return this.client;
        }

        public void setClient(Client client)
        {
            this.client = client;
        }
    }

    public class Request
    {
        private BsonDocument requestDoc;

        public Request(BsonDocument request)
        {
            this.requestDoc = request;
        }

        protected Request() { }

        protected BsonDocument Process(Storage storage, Connections connections)
        {
            return null;
        }

        public BsonDocument GetResponse(Storage storage, Connections connections)
        {
            string requestType = requestDoc.GetValue("request_type").AsString;

            Request request = null;

            switch (requestType)
            {
                case "HANDSHAKE_REQUEST":
                    request = new HandshakeRequest(requestDoc.GetValue("host").AsString,
                                                   requestDoc.GetValue("pid").AsInt64,
                                                   requestDoc.GetValue("local_port").AsInt32);
                    break;
                case "SYNC_STORAGE_REQUEST":
                    request = new SyncStorageRequest(requestDoc.GetValue("storage").AsBsonDocument);
                    break;
                case "PUT_REQUEST":
                    request = new PutRequest(requestDoc.GetValue("name").AsString,
                                            requestDoc.GetValue("value").AsBsonDocument);
                    break;
                case "INCOMING_DATA_REQUEST":
                    request = new IncomingDataRequest();
                    break;
                default: throw new Exception("Unexpected request type.");
            }

            return request.Process(storage, connections);
        }
    }

    public class HandshakeRequest : Request
    {
        private string host;
        private Int64 pid;
        private Int32 localPort;
        public HandshakeRequest(string host, Int64 pid, Int32 localPort)
        {
            this.host = host;
            this.pid = pid;
            this.localPort = localPort;
        }

        public new BsonDocument Process(Storage storage, Connections connections)
        {
            string handle = System.Guid.NewGuid().ToString();
            string dateTime = System.DateTime.Now.ToString("dddd , MMM dd yyyy,hh:mm:ss");

            connections.Add(handle, new Connection(host, (int)pid, localPort, dateTime, null));

            BsonDocument responseDoc = new BsonDocument();
            responseDoc.Add(new BsonElement("response_type", "HANDSHAKE_RESPONSE")).
                        Add(new BsonElement("connection_handle", handle));

            return responseDoc;
        }
    }

    public class PutRequest : Request
    {
        private string name;
        private BsonDocument value;

        public PutRequest(string name, BsonDocument value) 
        { 
            this.name = name; 
            this.value = value; 
        }

        public new BsonDocument Process(Storage storage, Connections connections)
        {
            storage.Put(name, value);

            BsonDocument responseDoc = new BsonDocument();
            responseDoc.Add(new BsonElement("response_type", "PUT_RESPONSE")).
                        Add(new BsonElement("result", "SUCCESS"));
            return responseDoc;
        }
    }

    public class SyncStorageRequest : Request {
        private Dictionary<string, BsonDocument> storageSnapshot;
        public SyncStorageRequest(BsonDocument storage) 
        {
            storageSnapshot = new Dictionary<string, BsonDocument>();
            foreach(BsonElement element in storage) 
            {
                storageSnapshot.Add(element.Name, element.Value.AsBsonDocument);
            }
        }

        public new BsonDocument Process(Storage storage, Connections connections)
        {
            storage.PutAllSerialized(storageSnapshot);

            BsonDocument responseDoc = new BsonDocument();
            responseDoc.Add(new BsonElement("response_type", "SYNC_STORAGE_RESPONSE")).
                        Add(new BsonElement("storage_snapshot", storage.getAllSerialized().ToBsonDocument()));
            return responseDoc;
        }
    }

    public class IncomingDataRequest : Request
    {
        public IncomingDataRequest() { }

        public new BsonDocument Process(Storage storage, Connections connections)
        {
            return null;
        }
    }

    public class Storage
    {
        private Dictionary<string, BsonDocument> dict;

        public Storage() 
        {
            this.dict = new Dictionary<string, BsonDocument>();
        }

        public void Put(string name, BsonDocument value)
        {
            Monitor.Enter(dict);
            if (!dict.ContainsKey(name))
            {
                dict.Add(name, value);
            }
            else
            {
                dict[name] = value;
            }
            Monitor.Exit(dict);
        }

        public void Put(string name, object value)
        {
            Monitor.Enter(dict);
            if (!dict.ContainsKey(name))
            {
                dict.Add(name, Converters.Serialize(value));
            }
            else
            {
                dict[name] = Converters.Serialize(value);
            }
            Monitor.Exit(dict);
        }

        public void PutAllSerialized(Dictionary<string, BsonDocument> dictSerialized)
        {
            Monitor.Enter(dict);
            foreach(KeyValuePair<string, BsonDocument> entry in dictSerialized)
            {
                this.Put(entry.Key, entry.Value);
            }
            Monitor.Exit(dict);
        }

        public void PutAll(Dictionary<string, object> dictToPut)
        {
            Monitor.Enter(dict);
            foreach (KeyValuePair<string, object> entry in dictToPut)
            {
                this.Put(entry.Key, Converters.Serialize(entry.Value));
            }
            Monitor.Exit(dict);
        }

        public BsonDocument GetSerialized(string name)
        {
            BsonDocument value = null;
            Monitor.Enter(dict);
            if (dict.ContainsKey(name))
            {
                value = dict[name];
            }
            Monitor.Exit(dict);
            return value;
        }

        public object Get(string name)
        {
            object value = null;
            Monitor.Enter(dict);
            if (dict.ContainsKey(name))
            {
                value = Converters.Deserialize(dict[name]);
            }
            Monitor.Exit(dict);
            return value;
        }

        public bool Contains(string name)
        {
            return dict.ContainsKey(name);
        }

        public Dictionary<string, BsonDocument> getAllSerialized()
        {
            Dictionary<string, BsonDocument> dictCopy = new Dictionary<string, BsonDocument>();
            Monitor.Enter(dict);
            foreach(KeyValuePair<string, BsonDocument> entry in dict)
            {
                dictCopy.Add(entry.Key, entry.Value);
            }
            Monitor.Exit(dict);
            return dictCopy;
        }

        public Dictionary<string, object> getAll()
        {
            Dictionary<string, object> dictCopy = new Dictionary<string, object>();
            Monitor.Enter(dict);
            foreach (KeyValuePair<string, BsonDocument> entry in dict)
            {
                dictCopy.Add(entry.Key, Converters.Deserialize(entry.Value));
            }
            Monitor.Exit(dict);
            return dictCopy;
        }
    }

    public class Converters
    {
        public static string getTypeMarker(object obj)
        {
            if (obj is int || obj is long || obj is Int64) return "int";
            else if (obj is Int32) return "int32";
            else if (obj is float || obj is double) return "float";
            else if (obj is string) return "string";
            else if (obj is IDictionary) return "dict";
            else if (obj is IList) return "list";
            else return "unknown";
        }

        public static BsonDocument Serialize(object value)
        {
            string typeMarker = getTypeMarker(value);
            

            if (!getTypeMarker(value).Equals("unknown"))
            {
                BsonDocument serializedObj = new BsonDocument(new BsonElement("value_type", typeMarker));

                switch (typeMarker)
                {
                    case "int":
                    case "int32":
                    case "float":
                    case "string":
                        return serializedObj.Add(new BsonElement("value", BsonTypeMapper.MapToBsonValue(value)));

                    case "list":
                        List<BsonDocument> serializedList = new List<BsonDocument>();
                        List<object> list = (List<object>) value;
                        foreach (object item in list)
                        {
                            serializedList.Add(Serialize(item));
                        }
                        return serializedObj.Add(new BsonElement("value", BsonTypeMapper.MapToBsonValue(serializedList)));

                    case "dict":
                        Dictionary<string, BsonDocument> serializedDict = new Dictionary<string, BsonDocument>();
                        Dictionary<string, object> dictionary = (Dictionary<string, object>) value;
                        foreach (KeyValuePair<string, object> entry in dictionary)
                        {
                            serializedDict.Add(entry.Key, Serialize(entry.Value));
                        }
                        return serializedObj.Add(new BsonElement("value", BsonTypeMapper.MapToBsonValue(serializedDict)));

                    default: return null;
                }
            }   
            BsonDocument doc = (BsonDocument) value;
            return doc;
        }

        public static object Deserialize(BsonDocument serializedValue) 
        {
            string valueType = serializedValue.GetValue("value_type").AsString;

            BsonValue value = serializedValue.GetValue("value");

            switch (valueType)
            {
                case "int":
                case "int32": 
                    return value.AsInt32;

                case "float":
                case "float64": 
                    return value.AsDouble;

                case "string": 
                    return value.AsString;

                case "list":
                case "tuple":
                case "array":
                    List<object> deserializedList = new List<object>();
                    BsonArray arr = value.AsBsonArray;
                    foreach (object elem in arr)
                    {
                        BsonDocument doc = elem.ToBsonDocument();
                        deserializedList.Add(Deserialize(doc));
                    }
                    return deserializedList;

                case "dict":
                    Dictionary<string, object> deserializedDict = new Dictionary<string, object>();
                    BsonDocument serialized = value.ToBsonDocument();
                    foreach (BsonElement elem in serialized)
                    {
                        string name = elem.Name;
                        BsonDocument v = elem.Value.ToBsonDocument();
                        deserializedDict.Add(name, Deserialize(v));
                    }
                    return deserializedDict;

                default:
                    Console.WriteLine("Cannot deserialize given object");
                    return null;
            }
        }

    }

    public class Sydx
    {
        private static int? serverPort;
        private Server server;
        private Storage storage;
        private Connections connections;

        public Sydx(Storage storage, Connections connections)
        {
            this.storage = storage;
            this.connections = connections;
        }

        public Client _connect(string host, int port)
        {
            Client client = new Client(host, port, (int)serverPort, storage);
            client.Connect();
            return client;
        }

        public string Connect(string host, int port)
        {
            if (serverPort is null)
            {
                Console.WriteLine("Must open port before connecting");
                return null;
            } else
            {
                Client client = _connect(host, port);
                //connections.Add(client.Handle(),
                //new Connection(host, client.Pid(), port, System.DateTime.Now.ToString("dddd , MMM dd yyyy,hh:mm:ss"), client));
                return client.Handle();
            }
        }

        public bool Port(int port)
        {
            if(serverPort is null)
            {
                serverPort = port;
                this.server = new Server("", port, storage, connections);

                Thread t = new Thread(server.run);
                t.Start();
                return true;
            } else
            {
                Console.WriteLine("Server port already open");
                return false;
            }
        }

        public void Port(string host, int port)
        {
            if (serverPort is null)
            {
                serverPort = port;
                server = new Server(host, port, storage, connections);

                Thread t = new Thread(server.run);
                t.Start();
            }
            else
            {
                Console.WriteLine("Server port already open");
            }
        }

        public void ClosePort()
        {
            if(serverPort is null)
            {
                Console.WriteLine("Server port is null");
            } else
            {
                server.stop();
                Console.WriteLine("Closed server port");
            }
        }

        public void Put(string name, object value)
        {
            string type = Converters.getTypeMarker(value);

            BsonDocument valueAndType = Converters.Serialize(value);
            BsonDocument putRequest = new BsonDocument(new BsonElement("request_type", "PUT_REQUEST")).
                                                   Add(new BsonElement("name", name)).
                                                   Add(new BsonElement("value", valueAndType));
            storage.Put(name, valueAndType);
            connections.SendToAll(putRequest, this);
        }

        public object Get(string name)
        {
            return storage.Get(name);
        }
    }

    public static class ExcelFunctions
    {
        private static Sydx sydx;
        private static readonly Storage storage = new Storage();
        private static Connections connections = new Connections();

        private static Boolean portOpen = false;

        [ExcelFunction(Description = "Opens a port for interaction with other Sydx systems")]
        public static string S_Port(
            [ExcelArgument(Name = "port", Description = "port number, e.g. 4782")] int port)
        {
            // if (!portOpen)
            // {
            //     ServerSydx server = new ServerSydx(port, storage);
            //     Thread serverThread = new Thread(server.Run);
            //     serverThread.Start();
            //     portOpen = true;
            //     return "`success";
            // }
            // else
            // {
            //     return "`failure(reason='port already open')";
            // }
            if (sydx is null)
            {
                sydx = new Sydx(storage, connections);
                bool isPortOpen = sydx.Port(port);
                return "`success";   
            }
            return "`failure(reason='port already open')";
        } 

        public static string S_Connect([ExcelArgument(Name = "host", Description = "Host to conenct to")] string host, 
            [ExcelArgument(Name = "port", Description = "Port to connect to")] int port)
        {
            return sydx.Connect(host, port);
        }

        [ExcelFunction(Description = "Returns the version of the Integral AddIn as a string, e.g. 1.0.0")]
        public static string S_Version()
        {
            return "1.0.0";
        }

        [ExcelFunction(Description = "Puts an object into the Integral dictionary")]
        public static string S_Put(string name, object value)
        {
            sydx.Put(name, value);
            return "`" + name + "(type=" + value.GetType().ToString() + ")";
        }

        [ExcelFunction(Description = "Gets an object from the Integral dictionary")]
        public static object S_Get(string name, object value)
        {
            return sydx.Get(name);
        }

        [ExcelFunction(Description = "Greets the user by name")]
        public static string IntegralHello(string name)
        {
            return "Hello " + name;
        }

        [ExcelFunction(Description = "An identity function: returns its argument")]
        public static object S_Identity(object arg)
        {
            return arg;
        }

        [ExcelFunction(Description = "Describes the value passed to the function")]
        public static string S_Describe(object arg)
        {
            if (arg is double)
                return "Double: " + (double)arg;
            else if (arg is string)
                return "String: " + (string)arg;
            else if (arg is bool)
                return "Boolean: " + (bool)arg;
            else if (arg is ExcelError)
                return "ExcelError: " + arg.ToString();
            else if (arg is object[,])
                // The object array returned here may contain a mixture of different types,
                // reflecting the different cell contents
                return string.Format("Array[{0},{1}]", ((object[,])arg).GetLength(0), ((object[,])arg).GetLength(1));
            else if (arg is ExcelMissing)
                return "<<Missing>>"; // Would have been System.Reflection.Missing in previous versions of ExcelDna
            else if (arg is ExcelEmpty)
                return "<<Empty>>"; // Would have been null
            else
                return "!? Unheard Of ?!";
        }
    }
}
