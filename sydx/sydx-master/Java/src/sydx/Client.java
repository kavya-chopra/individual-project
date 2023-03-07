package sydx;

import sydx.RequestTypes.Request;

public class Client {

  private Object host;
  private int port;
  private Integer localPort;
  private Storage storage;
  private Object handle;

  public Client(Object host, int port, Integer localPort, Storage storage){
    this.host = host;
    this.port = port;
    this.localPort = localPort;
    this.storage = storage;
    this.handle = null;
  }

  public void sendRequest(Request request){
    //TODO
  }

  public void connect(){
    //TODO
  }

  public Object getHandle(){
    return this.handle;
  }
}