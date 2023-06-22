package zend.RequestTypes;

import org.bson.Document;
import zend.Connections;
import zend.Storage;
import zend.ZendException;

import java.util.Map;

public class SyncStorageRequest extends Request {

  private Map<String, Document> storageSnapshot;

  public SyncStorageRequest(Map<String, Document> storageSnapshot){
    this.storageSnapshot = storageSnapshot;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections) throws ZendException {
    storage.putAllFromSerializedMap(storageSnapshot);

    responseDoc.append("response_type", "SYNC_STORAGE_RESPONSE");
    responseDoc.append("storage_snapshot", storage.getAllSerialized());
    return responseDoc;
  }
}