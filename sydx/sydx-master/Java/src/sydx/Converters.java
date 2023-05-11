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
      case "Integer" : type = "int";
        break;
      case "Float" : type = "float";
        break;
      case "String" : type = "string";
        break;
      case "Hashmap" : type = "dict";
        break;
      case "ArrayList" : type = "list";
        break;
      default : type = "unknown";
        new SydxException("Not a typical datatype");
    }
    return type;
  }

  public static Document serialize(Object obj) {
    String typeMarker = getTypeMarker(obj);

    Document serialized = new Document();

    if (!typeMarker.equals("unknown")) {
      serialized.append("value_type", typeMarker);

      switch (typeMarker) {
        case "int" :
        case "float" :
        case "string" :
          serialized.append("value", obj);
          break;

        case "dict" :
          Map dict = (HashMap<String, Object>) obj;
          Map dictSerialized = new HashMap<String, Document>();
          dict.forEach((key, value) -> dictSerialized.put(key, serialize(value)));
          serialized.append("value", dictSerialized);
          break;

        case "list" :
          List listSerialized = new ArrayList<Document>();
          List list = (ArrayList<Object>) obj;
          for (Object value : list) {
            Document serializedVal = serialize(value);
            listSerialized.add(serializedVal);
          }
          serialized.append("value", listSerialized);
          break;

        default : System.out.println("Uncommon type object to be serialized");
      }

    } else {
      Document uncommonTypeObj = (Document) obj;
      String valueType = uncommonTypeObj.getString("value_type");
      serialized.append("value_type", valueType);

      if (valueType.equals("dict")) {
        Map dictSerialized = new HashMap<Document, Document>();
        Map dict = uncommonTypeObj.get("value", new HashMap<String, Object>());
        dict.forEach((key, value) -> dictSerialized.put(key, serialize(value)));
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
    String valueType = json_obj.getString("value_type");

    switch (valueType) {
      case "int":
      case "int32":
      case "float":
      case "float64":
      case "string":
        return json_obj.get("value");

      case "list":
      case "tuple":
      case "array":
        List<Object> deserializedList = new ArrayList<>();
        List<Document> serializedList = json_obj.get("value", new ArrayList<Document>());
        for (Document serializedValue : serializedList) {
          deserializedList.add(deserialize(serializedValue));
        }
        return deserializedList;

      case "dict":
        Map<String, Object> deserializedMap = new HashMap<>();
        Map<String, Document> serializedMap = json_obj.get("value", new HashMap<String, Document>());
        for (Map.Entry<String, Document> entry : serializedMap.entrySet()) {
          deserializedMap.put(entry.getKey(), deserialize(entry.getValue()));
        }
        return deserializedMap;

      default: throw new SydxException("Cannot deserialize given json object");
    }
  }
}
