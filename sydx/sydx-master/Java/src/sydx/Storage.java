package sydx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
  private Map<String, Object> dict;
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

  public void put(String name, Object value){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.put(name, value);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public Object get(String name){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Object value = null;
    if(dict.containsKey(name)){
      value = dict.get(name);
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return value;
  }

  public Map<String, Object> getAll(){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Map<String, Object> dictCopy = new HashMap<>();
    for (Map.Entry<String, Object> entry : dict.entrySet()) {
      dictCopy.put(entry.getKey(), entry.getValue());
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return dictCopy;
  }

  public void putAll(Map<String, Object> toAdd){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.putAll(toAdd);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }
}