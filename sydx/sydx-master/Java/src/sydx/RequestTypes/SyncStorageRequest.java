package sydx.RequestTypes;

import org.bson.Document;
import sydx.Connections;
import sydx.Storage;

import java.util.Map;

public class SyncStorageRequest extends Request {

  private Map<String, Object> storageSnapshot;

  public SyncStorageRequest(Map<String, Object> storageSnapshot){
    this.storageSnapshot = storageSnapshot;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections){
    storage.putAll(storageSnapshot);

    responseDoc.append("response_type", "SYNC_STORAGE_RESPONSE");
    responseDoc.append("our_storage_snapshot", storage.getAll());
    return responseDoc;
  }
}