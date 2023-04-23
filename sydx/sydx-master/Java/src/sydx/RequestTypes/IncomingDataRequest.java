package sydx.RequestTypes;

import org.bson.Document;
import sydx.Connections;
import sydx.Storage;

public class IncomingDataInterpreter extends Interpreter {

  private String functionName;
  private Object data;

  public IncomingDataInterpreter(String functionName, Object data){
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
      default: System.out.println("Function not recognized by Java component.");
    }

    return response;
  }
}