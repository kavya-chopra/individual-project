package sydx;

import org.bson.Document;
import sydx.RequestTypes.Request;

import java.io.IOException;
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

  public synchronized void sendToAll(Document request) throws IOException {
    for (Map.Entry<Object, Connection> connectionEntry : connections.entrySet()) {
      Connection connection = connectionEntry.getValue();
      if (connection.getClient() == null){
        connection.setClient(_connect(connection.getHost(), connection.getLocal_port()));
      }
      connection.getClient().sendRequest(request);
    }
  }
}