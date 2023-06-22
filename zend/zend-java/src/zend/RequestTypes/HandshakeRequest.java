package zend.RequestTypes;

import java.util.UUID;

import org.bson.Document;
import zend.Connection;
import zend.Storage;
import zend.Connections;

public class HandshakeRequest extends Request {

  private final String host;
  private final Integer pid;
  private final int localPort       ;

  public HandshakeRequest(String host, Integer pid, int localPort) {
    this.host = host;
    this.pid = pid;
    this.localPort = localPort;
  }

  public String getHost(){
    return host;
  }

  public Integer getPid() {
    return pid;
  }

  public int getLocalPort() {
    return localPort;
  }

  @Override
  public Document processRequest(Storage storage, Connections connections) {
    String handle = UUID.randomUUID().toString();
//    Instant timeNow = Instant.now();
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    connections.add(handle, new Connection(host, pid, localPort, java.time.LocalDateTime.now(), null));

    responseDoc.append("response_type", "HANDSHAKE_RESPONSE");
    responseDoc.append("connection_handle", handle);
    return responseDoc;
  }
}
