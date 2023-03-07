package sydx.RequestTypes;

import sydx.Connections;
import sydx.Storage;

import java.util.HashMap;
import java.util.Map;

public class PutRequest extends Request{
  private Object name;
  private Object value;

  public PutRequest(Object name, Object value){
    this.name = name;
    this.value = value;
    this.response = new HashMap<>();
  }

  @Override
  public Map<String, Object> processRequest(Storage storage, Connections connections) {
    storage.put(name, value);

    response.put("response_type", "PUT_RESPONSE");
    response.put("result", "SUCCESS");
    return response;
  }
}