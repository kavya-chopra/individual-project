package sydx;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
  private Map<String, String> dict;
  Lock lock;
  Logger log = Logger.getLogger(Storage.class.getName());

  public Storage(){
    this.dict = new HashMap<>();
    lock = new ReentrantLock();
  }

  public boolean contains(String name){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    boolean exists = dict.containsKey(name);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return exists;
  }

  public void put(String name, String valueAndType){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.put(name, valueAndType);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public String get(String name){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    String value = null;
    if(dict.containsKey(name)){
      value = dict.get(name);
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return value;
  }

  public Map<String, String> getAll(){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Map<String, String> dictCopy = new HashMap<>();
    for (Map.Entry<String, String> entry : dict.entrySet()) {
      dictCopy.put(entry.getKey(), entry.getValue());
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return dictCopy;
  }

  public void putAllFromMap(Map<String, Object> toAdd){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    for (Map.Entry<String, Object> entry : toAdd.entrySet()) {
      String valueAndType = new Document("value_type", Sydx.getTypeMarker(entry.getValue()))
                            .append("value", entry.getValue())
                            .toJson();
      dict.put(entry.getKey(), valueAndType);
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public void putAll(Map<String, String> toAdd){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.putAll(toAdd);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }
}