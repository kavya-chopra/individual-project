import logging

import numpy as np

import zend

def numpy_numeral_to_json_object(obj):
    if isinstance(obj, np.int32):
        return {
            'value_type': 'int32',
            'value': int(obj)
        }
    elif isinstance(obj, np.float64):
        return {
            'value_type': 'float64',
            'value': float(obj)
        }
    else:
        raise ValueError('Unable to serialise an object')

def numpy_numeral_from_json_object(json_object):
    if isinstance(json_object, dict):
        if 'value_type' in json_object and json_object['value_type'] == 'int32':
            return np.int32(json_object['value'])
        elif 'value_type' in json_object and json_object['value_type'] == 'float64':
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
            result.append(zend.__serialise(x))
    return result

def __list_to_numpy_array(lst):
    result = []
    for x in lst:
        if isinstance(x, list):
            result.append(__list_to_numpy_array(x))
        else:
            result.append(zend.__deserialise(x))
    return np.array(result)

def numpy_array_to_json_object(obj):
    if not isinstance(obj, np.ndarray):
        raise ValueError('Unable to serialise an object')
    return {
        'value_type': 'array',
        'value': __numpy_array_to_list(obj)
    }

def numpy_array_from_json_object(json_object):
    if isinstance(json_object, list): 
        if 'value_type' in json_object and json_object['value_type'] == 'array':
            return __list_to_numpy_array(json_object['value'])
        else:
            raise ValueError('Unable to deserialise an object')
    raise ValueError('Unable to deserialise an object')

def plain_old_data_to_json_object(obj):
    logger = logging.getLogger()
    logger.debug('Attempting to serialise a plain old data object')
    if isinstance(obj, int):
        return {
            'value_type': 'int',
            'value': obj
        }
    elif isinstance(obj, float):
        return {
            'value_type': 'float',
            'value': obj
        } 
    elif isinstance(obj, str):
        return {
            'value_type': 'string',
            'value': obj
        }
    elif isinstance(obj, list):
        return {
            'value_type': 'list',
            'value': [zend.__serialise(x) for x in obj]
        }
    # if isinstance(obj, (int, float, complex)):
    #     return obj
    # elif isinstance(obj, str):
    #     return obj
    # elif isinstance(obj, list):
    #     return [sydx.__serialise(x) for x in obj]
    elif isinstance(obj, tuple):
        return {
            'value_type': 'tuple',
            'value': [zend.__serialise(x) for x in list(obj)]
            # 'value': list(obj)
        }
    elif isinstance(obj, dict):
        return {
            'value_type': 'dict',
            'value': {x: zend.__serialise(y) for x, y in obj.items()}
        }
    elif obj is None:
        return None
    else:
        logger.debug('This object does not seem to be a plain old data object')
        raise ValueError('Unable to serialise an object')

def plain_old_data_from_json_object(json_object):
    logger = logging.getLogger()
    logger.debug('Attempting to deserialise a plain old data object')
    # if isinstance(json_object, (int, float, complex)):
    #     return json_object
    # elif isinstance(json_object, str):
    #     return json_object
    # elif isinstance(json_object, list):
    #     return [sydx.__deserialise(x) for x in json_object]
    # elif isinstance(json_object, tuple):
    #     return tuple([sydx.__deserialise(x) for x in json_object])
    if isinstance(json_object, dict):
        if 'value_type' in json_object:
            if json_object['value_type'] == 'int':
                return json_object['value']
            elif json_object['value_type'] == 'float':
                return json_object['value']
            elif json_object['value_type'] == 'string':
                return json_object['value']
            elif json_object['value_type'] == 'list':
                return [zend.__deserialise(x) for x in json_object['value']]
            elif json_object['value_type'] == 'tuple':
                return tuple([zend.__deserialise(x) for x in json_object['value']])
            elif json_object['value_type'] == 'dict':
                return {x: zend.__deserialise(y) for x, y in json_object['value'].items()}
        else:
            logger.debug('This JSON object does not seem to represent a plain old data object')
            raise ValueError('Unable to deserialise an object')
    elif json_object is None:
        return None
    else:
        logger.debug('This JSON object does not seem to represent a plain old data object')
        raise ValueError('Unable to deserialise an object')
