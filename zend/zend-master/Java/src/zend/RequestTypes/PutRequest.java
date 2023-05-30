package zend.RequestTypes;

import org.bson.Document;
import zend.Connections;
import zend.Storage;

public class PutRequest extends Request {
  private String name;
  private Document value;

  public PutRequest(String name, Document value){
    this.name = name;
    this.value = value;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections) {
    storage.put(name, value);

    responseDoc.append("response_type", "PUT_RESPONSE");
    responseDoc.append("result", "SUCCESS");
    return responseDoc;
  }
}