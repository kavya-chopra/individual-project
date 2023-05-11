package sydx;

import org.bson.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
  private Map<String, Document> dict;
  Lock lock;
  Logger log = Logger.getLogger(Storage.class.getName());

  public Storage(){
    this.dict = new HashMap<>();
    lock = new ReentrantLock();
  }

  public String getStorageAsString() {
    return new Document(dict).toJson();
  }

  public boolean contains(String name){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    boolean exists = dict.containsKey(name);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return exists;
  }

  public void put(String name, Document value) {
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.put(name, value);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public void put(String name, Object value){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.put(name, Converters.serialize(value));
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public Object get(String name) throws SydxException {
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Object value = null;
    if(dict.containsKey(name)){
      value = Converters.deserialize(dict.get(name));
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return value;
  }

  public Document getSerialized(String name) {
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Document value = null;
    if(dict.containsKey(name)){
      value = dict.get(name);
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return value;
  }

  public Map<String, Document> getAllSerialized(){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Map<String, Document> dictCopy = new HashMap<>();
    for (Map.Entry<String, Document> entry : dict.entrySet()) {
      dictCopy.put(entry.getKey(), entry.getValue());
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return dictCopy;
  }

  public Map<String, Object> getAll() throws SydxException {
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Map<String, Object> dictCopy = new HashMap<>();
    for (Map.Entry<String, Document> entry : dict.entrySet()) {
      dictCopy.put(entry.getKey(), Converters.deserialize(entry.getValue()));
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return dictCopy;
  }

  public void putAllFromSerializedMap(Map<String, Document> toAdd){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    for (Map.Entry<String, Document> entry : toAdd.entrySet()) {
      dict.put(entry.getKey(), entry.getValue());
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public void putAllFromMap(Map<String, Object> toAdd){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    for (Map.Entry<String, Object> entry : toAdd.entrySet()) {
      dict.put(entry.getKey(), Converters.serialize(entry.getValue()));
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

//  public void putAll(Map<String, Object> toAdd) {
//    log.log(Level.INFO, "Acquiring storage lock");
//    lock.lock();
//    for (Map.Entry<String, Object> entry : toAdd.entrySet()) {
//      String name = entry.getKey();
//      Object value = entry.getValue();
////      String typeMarker = Converters.getTypeMarker(entry.getValue());
////
////      if (!typeMarker.equals("unknown")) {
////        // value is a plain old object
////        dict.put(name, new Document("value_type", typeMarker).append("value", entry.getValue()));
////      } else {
////        // value is a compilation eg: list, dictionary
////        Document tryDeserialize = (Document) entry.getValue();
////        Document value = new Document("value_type", tryDeserialize.getString("value_type"));
////
////        if (tryDeserialize.getString("value_type").equals("dict")) {
////          value.append("value", tryDeserialize.get("value", new HashMap<String, Object>()));
////        } else if (tryDeserialize.getString("value_type").equals("list") ||
////                   tryDeserialize.getString("value_type").equals("tuple")) {
////          value.append("value", tryDeserialize.get("value", new ArrayList()));
////        } else {
////          return;
////        }
//
//        dict.put(name, Converters.serialize(value));
//      }
//
//
////      String[] valueStr = entry.getValue().substring(1, entry.getValue().length() - 1).split(":,");
////      String valueType = valueStr[1].substring(1, valueStr[1].length() - 1);
////
////      Document docToAdd = new Document("value_type", valueType);
////      switch (valueType) {
////        case "int32" : docToAdd.append("value", Integer.parseInt(valueStr[3]));
////        break;
////        case "float64" : docToAdd.append("value", Double.parseDouble(valueStr[3]));
////        break;
////        case "string" : docToAdd.append("value", valueStr[3]);
////        break;
////        case "dict" : docToAdd.append("value", valueStr[3]);
////        break;
////        case "array" : docToAdd.append("value", valueStr[3].substring(1, valueStr[3].length() - 1).split(", "));
////        break;
////        default : throw new SydxException("Unsupported value type when adding to storage");
////      }
//
//      // dict.put(name, docToAdd.toJson());
//
//    // }
//    log.log(Level.INFO, "Releasing storage lock");
//    lock.unlock();
//  }
}