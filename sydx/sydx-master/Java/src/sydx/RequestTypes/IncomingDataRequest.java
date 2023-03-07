package sydx.RequestTypes;

import sydx.Connections;
import sydx.Storage;

import java.util.HashMap;
import java.util.Map;

public class IncomingDataRequest extends Request{

  private String functionName;
  private Object data;

  public IncomingDataRequest(String functionName, Object data){
    this.functionName = functionName;
    this.data = data;
    this.response = new HashMap<>();
  }

  public String getFunctionName() {
    return functionName;
  }

  public Object getData() {
    return data;
  }

  @Override
  public Map<String, Object> processRequest(Storage storage, Connections connections) {
    //TODO
    return response;
  }
}