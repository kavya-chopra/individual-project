package sydx;

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converters {

  public static String getTypeMarker(Object value) {
    Class<?> valueClassStr = value.getClass();
    String valueClass = valueClassStr.toString().substring(valueClassStr.toString().lastIndexOf('.') + 1);
    String type = null;
    switch (valueClass) {
      case "Integer" : type = "int32";
        break;
      case "Float" : type = "float64";
        break;
      case "String" : type = "string";
        break;
//      case "Hashmap" : // merged with case "Dictionary"
//      case "Dictionary" : type = "dict";
//        break;
//      case "ArrayList" : type = "list";
//        break;
      default : type = "unknown";
        new SydxException("Not a typical datatype");
    }
    return type;
  }

  public static Document serialize(Object obj) {
    String typeMarker = getTypeMarker(obj);

    Document serialized = new Document();

    if (!typeMarker.equals("unknown")) {
      serialized.append("value_type", typeMarker).append("value", obj);

    } else {
      Document uncommonTypeObj = (Document) obj;
      String valueType = uncommonTypeObj.getString("value_type");
      serialized.append("value_type", valueType);

      if (valueType.equals("dict")) {
        Map dictSerialized = new HashMap<Document, Document>();
        Map dict = uncommonTypeObj.get("value", new HashMap<Object, Object>());
        dict.forEach((key, value) -> {
          Document name = serialize(key);
          Document serializedValue = serialize(value);
          dictSerialized.put(name, serializedValue);
        });
        serialized.append("value", dictSerialized);

      } else if (valueType.equals("list") || valueType.equals("tuple") || valueType.equals("array")) {
        List listSerialized = new ArrayList<Document>();
        List list = uncommonTypeObj.get("value", new ArrayList<Object>());
        for (Object value : list) {
          Document serializedVal = serialize(value);
          listSerialized.add(serializedVal);
        }
        serialized.append("value", listSerialized);
      }
    }
    return serialized;
  }

  public static Object deserialize(Document json_obj) throws SydxException {
    String typeMarker = Converters.getTypeMarker(json_obj);

    if (!typeMarker.equals("unknown")) {
      // value is a plain old object
      return json_obj.get("value");
    } else {
      // value is a compilation eg: list, dictionary
      Document tryDeserialize = (Document) json_obj;
      String valueType = tryDeserialize.getString("value_type");
      Document value = new Document("value_type", valueType);

      if (valueType.equals("dict")) {
        Map<Object, Object> deserialized = new HashMap<>();
        Map<Document, Document> serializedMap = tryDeserialize.get("value", new HashMap<Document, Document>());
        for (Map.Entry<Document, Document> entry : serializedMap.entrySet()) {
          deserialized.put(deserialize(entry.getKey()), deserialize(entry.getValue()));
        }
        return deserialized;

      } else if (valueType.equals("list") || valueType.equals("tuple") || valueType.equals("array")) {
        List<Object> deserialized = new ArrayList<>();
        List<Document> serializedList = tryDeserialize.get("value", new ArrayList<Document>());
        for (Document serializedValue : serializedList) {
          deserialized.add(deserialize(serializedValue));
        }
        return deserialized;
      } else {
        throw new SydxException("Trying to deserialize unsupported data type");
      }
    }
  }
}
