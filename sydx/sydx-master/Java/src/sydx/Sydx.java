package sydx;

public class Sydx {

  private static Integer server_port;
  private static Storage storage = new Storage();
  private static Connections connections = new Connections();

  public static Client _connect(String host, int localPort){
    Client client = new Client(host, localPort, server_port, storage);
    client.connect();
    return client;
  }

  public static void setServerPort(){
    //TODO
  }

  public static String connect(String host, int port) throws SydxException {
    if(server_port == null){
      throw new SydxException("Must open port before connecting");
    }
    return _connect(host, port).getHandle();
  }

  public static Integer getServerPort(){
    return server_port;
  }

  public void serialise(){
    //TODO
  }

  public void deserialize(){
    //TODO
  }

  public void put(){
    //TODO
  }

  public void get(){
    //TODO
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