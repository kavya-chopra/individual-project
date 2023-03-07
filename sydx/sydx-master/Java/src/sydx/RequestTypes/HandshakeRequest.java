package sydx.RequestTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import sydx.Connection;
import sydx.Storage;
import sydx.Connections;

public class HandshakeRequest extends Request {

  private final String host;
  private final String pid;
  private final int local_port;

  public HandshakeRequest(String host, String pid, int local_port) {
    this.host = host;
    this.pid = pid;
    this.local_port = local_port;
    this.response = new HashMap<>();
  }

  public String getHost(){
    return host;
  }

  public String getPid() {
    return pid;
  }

  public int getLocal_port() {
    return local_port;
  }

  @Override
  public Map<String, Object> processRequest(Storage storage, Connections connections) {
    //TODO
    String handle = UUID.randomUUID().toString();
    Instant timeNow = Instant.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    connections.add(handle, new Connection(host, pid, local_port, formatter.format(timeNow), null));

    response.put("response_type", "HANDSHAKE_RESPONSE");
    response.put("connection_handle", handle);
    return response;
  }
}
