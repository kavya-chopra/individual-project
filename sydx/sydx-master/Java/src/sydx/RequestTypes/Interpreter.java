package sydx.RequestTypes;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import sydx.Connections;
import sydx.Storage;
import sydx.SydxException;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.bson.BsonDocument;
import org.bson.BsonReader;
import org.bson.Document;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;


public class Request
{
  public String request_type;
  public static Document response;
  //public Map<String, Object> response;
  protected Socket _handler;

  public Document processRequest(Storage storage, Connections connections)
  {
    return null;
  }

  public static byte[] deserializeRequest(Socket handler, String content, Storage storage, Connections connections) throws SydxException, IOException {
    //TODO: convert byte array bsonBytes to Document or figure out BsonDocument
    //TODO: convert Document response to byte array (bson) or figure out BsonDocument
    //TODO: fix PUT_REQUEST so that it can accept any value type not just string
    //TODO: figure out SYNC_STORAGE_REQUEST to find out the datatype of the values

    //Request r = JsonConvert.DeserializeObject<Request>(content);
    Request r;
    Request typedRequest;
    byte[] byteResponse;

    response = new Document();

    InputStream inputStream = handler.getInputStream();
    byte[] bsonBytes = inputStream.readAllBytes();

    Document bsonDoc = Document.parse(bsonBytes);

    switch(bsonDoc.getString("request_type"))
    {
      case "HANDSHAKE_REQUEST":
        typedRequest = new HandshakeRequest(bsonDoc.getString("host"),
                                            bsonDoc.getString("pid"),
                                            bsonDoc.getInteger("local_port"));

        break;
      case "SYNC_STORAGE_REQUEST":
        //typedRequest = new SyncStorageRequest<>();
        break;
      case "PUT_REQUEST":
        typedRequest = new PutRequest(bsonDoc.getString("name"), bsonDoc.get("value"));
        break;
      case "INCOMING_DATA_REQUEST":
        typedRequest = new IncomingDataRequest(bsonDoc.getString("function_name"), bsonDoc.get("data"));
        break;
      default:
        throw new SydxException("Unexpected request type");
        break;
    }

    typedRequest._handler = handler;
    response = typedRequest.processRequest(storage, connections);
    byteResponse = response.toBson();
    return byteResponse;
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