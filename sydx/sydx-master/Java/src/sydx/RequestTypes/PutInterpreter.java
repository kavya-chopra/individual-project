package sydx.RequestTypes;

import org.bson.Document;
import sydx.Connections;
import sydx.Storage;

public class PutRequest extends Request{
  private String name;
  private Object value;

  public PutRequest(String name, Object value){
    this.name = name;
    this.value = value;
    //this.response = new HashMap<>();
  }

  @Override
  public Document processRequest(Storage storage, Connections connections) {
    storage.put(name, value);

    response.append("response_type", "PUT_RESPONSE");
    response.append("result", "SUCCESS");
    return response;
  }
}