package sydx.RequestTypes;

import java.util.UUID;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.bson.Document;
import sydx.Connection;
import sydx.Storage;
import sydx.Connections;

public class HandshakeRequest extends Request {

  private final String host;
  private final Integer pid;
  private final int local_port;

  public HandshakeRequest(String host, Integer pid, int local_port) {
    this.host = host;
    this.pid = pid;
    this.local_port = local_port;
  }

  public String getHost(){
    return host;
  }

  public Integer getPid() {
    return pid;
  }

  public int getLocal_port() {
    return local_port;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections) {
    String handle = UUID.randomUUID().toString();
    Instant timeNow = Instant.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    connections.add(handle, new Connection(host, pid, local_port, formatter.format(timeNow), null));

    responseDoc.append("response_type", "HANDSHAKE_RESPONSE");
    responseDoc.append("connection_handle", handle);
    return responseDoc;
  }
}
