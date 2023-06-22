package zend;

import org.bson.Document;

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

  public Object get(String name) throws ZendException {
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

  public Map<String, Object> getAll() throws ZendException {
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
}