package zend;

import org.bson.Document;
import java.io.IOException;
import java.time.LocalDateTime;

public class Zend {

  private static Integer serverPort;
  private static Server server;
  private static Storage storage = new Storage();
  private static Connections connections = new Connections();

  public Zend() {
    storage = new Storage();
    connections = new Connections();
  }

  public static void openPort(Integer port) throws ZendException {
    if (serverPort != null){
      throw new ZendException("Port already open");
    }
    serverPort = port;
    server = new Server("", port, storage, connections);

    Thread t = new Thread(server);
    t.start();
  }

  public static void closePort() throws ZendException {
    if (serverPort == null) {
      throw new ZendException("Must open port before closing");
    }
    server.stop();
    System.out.println("Closed port");
  }

  static Client _connect(String host, int localPort){
    Client client = new Client(host, localPort, serverPort, storage);
    client.connect();
    connections.add(client.getHandle(), new Connection(host, Math.toIntExact(client.getPid()), localPort, LocalDateTime.now(), client));
    return client;
  }

  public static String connect(String host, int port) throws ZendException {
    if(serverPort == null){
      throw new ZendException("Must open port before connecting");
    }
    return _connect(host, port).getHandle();
  }

  public static Integer getServerPort(){
    return serverPort;
  }

  public static void put(String name, Object value){

    String type = Converters.getTypeMarker(value);

    Document valueAndType = new Document("value_type", type).append("value", value);

    Document request = new Document("request_type", "PUT_REQUEST")
            .append("name", name)
            .append("value", valueAndType);

    try {
      connections.sendToAll(request);
      storage.put(name, value);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }



  public static Object get(String name){
    try {
      return storage.get(name);
    } catch (ZendException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getStorageAsString() {
    return storage.getStorageAsString();
  }
}