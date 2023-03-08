package sydx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
  private Map<Object, Object> dict;
  Lock lock;
  Logger log = Logger.getLogger(Storage.class.getName());

  public Storage(){
    this.dict = new HashMap<>();
    lock = new ReentrantLock();
  }

  public boolean contains(Object name){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    boolean exists = dict.containsKey(name);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return exists;
  }

  public void put(Object name, Object value){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.put(name, value);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }

  public Object get(Object name){
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

  public Map<Object, Object> getAll(){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    Map<Object, Object> dictCopy = new HashMap<>();
    for (Map.Entry<Object, Object> entry : dict.entrySet()) {
      dictCopy.put(entry.getKey(), entry.getValue());
    }
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
    return dictCopy;
  }

  public void putAll(Map<Object, Object> toAdd){
    log.log(Level.INFO, "Acquiring storage lock");
    lock.lock();
    dict.putAll(toAdd);
    log.log(Level.INFO, "Releasing storage lock");
    lock.unlock();
  }
}