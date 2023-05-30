package zend;

import org.bson.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static zend.Sydx._connect;

public class Connections {
  private Map<String, Connection> connections;

  public Connections() {
    connections = new HashMap<>();
  }

  public synchronized void add(String handle, Connection connection) {
    connections.put(handle, connection);
  }

  public synchronized void sendToAll(Document request) throws IOException {
    for (Map.Entry<String, Connection> connectionEntry : connections.entrySet()) {
      Connection connection = connectionEntry.getValue();
      if (connection.getClient() == null){
        connection.setClient(_connect(connection.getHost(), connection.getLocalPort()));
      }
      connection.getClient().sendRequest(request);
    }
  }
}