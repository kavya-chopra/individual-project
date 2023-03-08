package sydx.RequestTypes;

import sydx.Connections;
import sydx.Storage;

import java.util.HashMap;
import java.util.Map;

public class SyncStorageRequest extends Request{

  private Map<Object, Object> storageSnapshot;

  public SyncStorageRequest(Map<Object, Object> storageSnapshot){
    this.storageSnapshot = storageSnapshot;
    this.response = new HashMap<>();
  }

  public Map<Object, Object> getStorageSnapshot(){
    return storageSnapshot;
  }

  @Override
  public Map<String, Object> processRequest(Storage storage, Connections connections){
    storage.putAll(storageSnapshot);

    response.put("response_type", "SYNC_STORAGE_RESPONSE");
    response.put("our_storage_snapshot", storage.getAll());
    return response;
  }
}