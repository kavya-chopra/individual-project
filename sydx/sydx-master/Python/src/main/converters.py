import logging

import numpy as np

import sydx

def numpy_numeral_to_json_object(obj):
    if isinstance(obj, np.int32):
        return {
            'type': 'numpy.int32',
            'value': int(obj)
        }
    elif isinstance(obj, np.float64):
        return {
            'type': 'numpy.float64',
            'value': float(obj)
        }
    else:
        raise ValueError('Unable to serialise an object')

def numpy_numeral_from_json_object(json_object):
    if isinstance(json_object, dict):
        if 'type' in json_object and json_object['type'] == 'numpy.int32':
            return np.int32(json_object['value'])
        elif 'type' in json_object and json_object['type'] == 'numpy.float64':
            return np.float64(json_object['value'])
        else:
            raise ValueError('Unable to deserialise an object')
    else:
        raise ValueError('Unable to deserialise an object')

def __numpy_array_to_list(arr):
    result = []
    for x in arr:
        if isinstance(x, np.ndarray):
            result.append(__numpy_array_to_list(x))
        else:
            result.append(sydx.__serialise(x))
    return result

def __list_to_numpy_array(lst):
    result = []
    for x in lst:
        if isinstance(x, list):
            result.append(__list_to_numpy_array(x))
        else:
            result.append(sydx.__deserialise(x))
    return np.array(result)

def numpy_array_to_json_object(obj):
    if not isinstance(obj, np.ndarray):
        raise ValueError('Unable to serialise an object')
    return {
        'type': 'numpy.ndarray',
        'values': __numpy_array_to_list(obj)
    }

def numpy_array_from_json_object(json_object):
    if isinstance(json_object, dict):
        if 'type' in json_object and json_object['type'] == 'numpy.ndarray':
            return __list_to_numpy_array(json_object['values'])
        else:
            raise ValueError('Unable to deserialise an object')
    raise ValueError('Unable to deserialise an object')

def plain_old_data_to_json_object(obj):
    logger = logging.getLogger()
    logger.debug('Attempting to serialise a plain old data object')
    if isinstance(obj, (int, float, complex)):
        return obj
    elif isinstance(obj, str):
        return obj
    elif isinstance(obj, list):
        return [sydx.__serialise(x) for x in obj]
    elif isinstance(obj, tuple):
        return {
            'type': 'tuple',
            'values': list(obj)
        }
    elif isinstance(obj, dict):
        return {
            'type': 'dict',
            'values': {sydx.__serialise(x): sydx.__serialise(y) for x, y in obj.items()}
        }
    elif obj is None:
        return None
    else:
        logger.debug('This object does not seem to be a plain old data object')
        raise ValueError('Unable to serialise an object')

def plain_old_data_from_json_object(json_object):
    logger = logging.getLogger()
    logger.debug('Attempting to deserialise a plain old data object')
    if isinstance(json_object, (int, float, complex)):
        return json_object
    elif isinstance(json_object, str):
        return json_object
    elif isinstance(json_object, list):
        return [sydx.__deserialise(x) for x in json_object]
    elif isinstance(json_object, tuple):
        return tuple([sydx.__deserialise(x) for x in json_object])
    elif isinstance(json_object, dict):
        if 'type' in json_object:
            if json_object['type'] == 'dict':
                return {sydx.__deserialise(x): sydx.__deserialise(y) for x, y in json_object['values'].items()}
            elif json_object['type'] == 'tuple':
                return tuple([sydx.__deserialise(x) for x in json_object['values']])
        else:
            logger.debug('This JSON object does not seem to represent a plain old data object')
            raise ValueError('Unable to deserialise an object')
    elif json_object is None:
        return None
    else:
        logger.debug('This JSON object does not seem to represent a plain old data object')
        raise ValueError('Unable to deserialise an object')
