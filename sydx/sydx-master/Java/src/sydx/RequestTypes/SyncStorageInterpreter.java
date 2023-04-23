package sydx.RequestTypes;

import org.bson.Document;
import sydx.Connections;
import sydx.Storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncStorageRequest<T> extends Request{

  private Map<String, T> storageSnapshot;

  public SyncStorageRequest(Map<String, T> storageSnapshot){
    this.storageSnapshot = storageSnapshot;
    //this.response = new HashMap<>();
  }

  public Map<String, T> getStorageSnapshot(){
    return storageSnapshot;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections){
    storage.putAll((Map<String, Object>) storageSnapshot);

    response.append("response_type", "SYNC_STORAGE_RESPONSE");
    response.append("our_storage_snapshot", storage.getAll());
    return response;
  }
}