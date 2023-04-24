package sydx;

import sydx.Connections;

public class Request {
  Storage storage;
  Connections connections;

  Request(Storage storage, Connections connections){
    this.storage = storage;
    this.connections = connections;
  }

  public void interpret(){
    //TODO
  }

}