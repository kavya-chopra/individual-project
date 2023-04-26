package sydx;

import org.bson.Document;

import java.io.IOException;

public class Sydx {

  private static Integer serverPort;
  private static Storage storage = new Storage();
  private static Connections connections = new Connections();

  public static Client _connect(String host, int localPort){
    Client client = new Client(host, localPort, serverPort, storage);
    client.connect();
    return client;
  }

  public static void port(Integer port){
    if (serverPort != null){
      new SydxException("Port already open");
      System.out.println("Port already open for java component");
    }
    serverPort = port;
    Server server = new Server("", port, storage, connections);

    Thread t = new Thread(server);
    t.start();
  }

  

  public void show(){
    //TODO
  }

  public void describe(){
    //TODO
  }

  public void represent(){
    //TODO
  }
}