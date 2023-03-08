package sydx.RequestTypes;

import java.net.Socket;
import java.util.Map;
import sydx.Connections;
import sydx.Storage;
import sydx.SydxException;

public class Request
{
  public String request_type;
  public Map<String, Object> response;
  protected Socket _handler;

  public Map<String, Object> processRequest(Storage storage, Connections connections)
  {
    return null;
  }

  public static Request deserializeRequest(Socket handler, String content) throws SydxException {
    //TODO: figure out how to deserialize from json/bson in Java

    //Request r = JsonConvert.DeserializeObject<Request>(content);
    Request typedRequest;

    switch(r.request_type)
    {
      case "HANDSHAKE_REQUEST":
        //typedRequest = JsonConvert.DeserializeObject<HandshakeRequest>(content);
        break;
      case "SYNC_STORAGE_REQUEST":
        //typedRequest = JsonConvert.DeserializeObject<SyncStorageRequest>(content);
        break;
      case "PUT_REQUEST":
        //typedRequest = JsonConvert.DeserializeObject<PutRequest>(content);
        break;
      case "INCOMING_DATA_REQUEST":
        //typedRequest = JsonConvert.DeserializeObject<IncomingDataRequest>(content);
        break;
      default:
        throw new SydxException("Unexpected request type");
        break;
    }

    typedRequest._handler = handler;
    return typedRequest;
  }

  public Socket getHandler() {
    return _handler;
  }

  public String getRequest_type() {
    return request_type;
  }

  public void setHandler(Socket _handler) {
    this._handler = _handler;
  }

  public void setRequest_type(String request_type) {
    this.request_type = request_type;
  }
}