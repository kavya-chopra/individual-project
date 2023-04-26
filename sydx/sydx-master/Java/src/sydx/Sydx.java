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

  public static String connect(String host, int port) throws SydxException {
    if(serverPort == null){
      throw new SydxException("Must open port before connecting");
    }
    return _connect(host, port).getHandle();
  }

  public static Integer getServerPort(){
    return serverPort;
  }

  public void put(String name, Object value){

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
      case "Integer" : type = "int";
        break;
      case "Float" : type = "float";
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

  public Object get(String name){
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