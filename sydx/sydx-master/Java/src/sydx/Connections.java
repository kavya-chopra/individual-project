package sydx;

import sydx.RequestTypes.Request;

import java.util.HashMap;
import java.util.Map;

import static sydx.Sydx._connect;

public class Connections {
  private Map<Object, Connection> connections;

  public Connections() {
    connections = new HashMap<>();
  }

  public synchronized void add(Object handle, Connection connection) {
    connections.put(handle, connection);
  }

  public synchronized void sendToAll(Request request){
    for (Map.Entry<Object, Connection> connectionEntry : connections.entrySet()) {
      Connection connection = connectionEntry.getValue();
      if (connection.getClient() == null){
        connection.setClient(_connect(connection.getHost(), connection.getLocal_port()));
      }
      connection.getClient().sendRequest(request);
    }
  }
}