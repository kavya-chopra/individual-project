package sydx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    //TODO
  }

  public void put(Object name, Object value){
    //TODO
  }

  public Object get(Object name){
    //TODO
  }

  public Map<Object, Object> getAll(){
    //TODO
  }

  public void putAll(Map<Object, Object> toAdd){
    //TODO
  }
}