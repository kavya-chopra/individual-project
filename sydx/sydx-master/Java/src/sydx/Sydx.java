package sydx;

import org.bson.Document;
import java.io.IOException;

public class Sydx {

  private static Integer serverPort;
  private static Server server;
  private static Storage storage = new Storage();
  private static Connections connections = new Connections();

  public static void port(Integer port) throws SydxException {
    if (serverPort != null){
      throw new SydxException("Port already open");
    }
    serverPort = port;
    server = new Server("", port, storage, connections);

    Thread t = new Thread(server);
    t.start();
  }

  public static void closePort() throws SydxException {
    if (serverPort == null) {
      throw new SydxException("Must open port before closing");
    }
    server.stop();
    System.out.println("Closed port");
  }

  static Client _connect(String host, int localPort){
    Client client = new Client(host, localPort, serverPort, storage);
    client.connect();
    return client;
  }

  public static String connect(String host, int port) throws SydxException {
    if(serverPort == null){
      throw new SydxException("Must open port before connecting");
    }
    return _connect(host, port).getHandle();
  }

  public static Integer getServerPort(){
    return serverPort;
  }

  public static void put(String name, Object value){

    String type = getTypeMarker(value);

    String valueAndType = new Document("value_type", type).append("value", value).toJson();

    Document request = new Document("request_type", "PUT_REQUEST")
            .append("name", name)
            .append("value", valueAndType);

    try {
      connections.sendToAll(request);
      storage.put(name, valueAndType);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getTypeMarker(Object value) {
    Class<?> valueClassStr = value.getClass();
    String valueClass = valueClassStr.toString().substring(valueClassStr.toString().lastIndexOf('.') + 1);
    String type = null;
    switch (valueClass) {
      case "Integer" : type = "int32";
        break;
      case "Float" : type = "float64";
        break;
      case "String" : type = "string";
      break;
      case "Hashmap" : // merged with case "Dictionary"
      case "Dictionary" : type = "dict";
        break;
      case "ArrayList" : type = "array";
        break;
      default :
        new SydxException("Unsupported value datatype");
        System.out.println("Cannot determine class");
    }
    return type;
  }

  public static Object get(String name){
    Document valueAndType = Document.parse(storage.get(name));
    return valueAndType.get("value");
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