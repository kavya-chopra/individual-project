package zend.RequestTypes;

import java.io.IOException;
import java.util.HashMap;

import org.bson.BasicBSONObject;
import zend.*;
import org.bson.Document;


public class Request
{
  public static Document responseDoc;
  protected static Document receivedDoc;

  public Request(Document receivedDoc){
    this.receivedDoc = receivedDoc;
    this.responseDoc = new Document();
  }

  protected Request(){}

  // To be overridden
  protected Document processRequest(Storage storage, Connections connections) throws ZendException {
    return null;
  }

  public static Document getResponse(Storage storage, Connections connections) throws ZendException, IOException {
    Request typedRequest;

    switch(receivedDoc.getString("request_type"))
    {
      case "HANDSHAKE_REQUEST":
        typedRequest = new HandshakeRequest(receivedDoc.getString("host"),
                                            receivedDoc.getInteger("pid"),
                                            receivedDoc.getInteger("local_port"));
        break;

      case "SYNC_STORAGE_REQUEST":
        typedRequest = new SyncStorageRequest(receivedDoc.get("storage", new HashMap<String, Document>()));
        break;

      case "PUT_REQUEST":
        BasicBSONObject value = (BasicBSONObject) receivedDoc.get(("value"));
        Document valueDoc = new Document("value_type", value.getString("value_type")).append("value", value.get("value"));
        // receivedDoc.get("value", new Document());
//        String valueAndType = new Document("value_type", Converters.getTypeMarker(value))
//                              .append("value", value)
//                              .toJson();
        typedRequest = new PutRequest(receivedDoc.getString("name"), valueDoc);
        break;

      case "INCOMING_DATA_REQUEST":
        typedRequest = new IncomingDataRequest(receivedDoc.getString("function_name"),
                                               receivedDoc.get("data", new HashMap<String, Object>()));
        break;

      default:
        throw new ZendException("Unexpected request type");
    }

    responseDoc = typedRequest.processRequest(storage, connections);
    return responseDoc;
  }
}