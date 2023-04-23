package sydx.RequestTypes;

import org.bson.Document;
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
    //this.response = new HashMap<>();
  }

  public String getFunctionName() {
    return functionName;
  }

  public Object getData() {
    return data;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections) {
    //TODO
    switch(functionName){
      case "simplex":
        break;
      default:
    }

    return response;
  }
}