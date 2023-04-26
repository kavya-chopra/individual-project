package sydx.RequestTypes;

import org.bson.Document;
import sydx.Connections;
import sydx.Storage;

public class PutRequest extends Request {
  private String name;
  private String value;

  public PutRequest(String name, String value){
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