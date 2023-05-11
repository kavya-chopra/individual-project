package sydx;

import org.bson.Document;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Client {

  private String host;
  private int port;
  private Integer localPort;
  private Storage storage;
  private Socket socket;
  private String handle;
  private Long pid;

  public Client(String host, int port, Integer localPort, Storage storage){
    this.host = host;
    this.port = port;
    this.localPort = localPort;
    this.storage = storage;
    this.handle = null;
  }

  public Document sendRequest(Document request) {

    Document receivedDoc = new Document();

    try {
      this.socket = new Socket(host, port);
      System.out.println("Java Client connected to server");

      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      byte[] requestBytes = BsonToBinaryAdapter.toBytes(request);
      outputStream.writeInt(requestBytes.length);
      outputStream.write(requestBytes);
      System.out.println("Sent request length: " + requestBytes.length
              + ". Sent request: " + request.toJson());

      DataInputStream inputStream = new DataInputStream(socket.getInputStream());
      int bsonDataLength = inputStream.readInt();
      byte[] bsonData = new byte[bsonDataLength];
      inputStream.readFully(bsonData);

      receivedDoc = BsonToBinaryAdapter.toDocument(bsonData);
      System.out.println("Received response: " + receivedDoc.toJson());
      this.socket.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    return receivedDoc;
  }

  public void connect(){

    Document requestHandshake = null;
    try {
      this.pid = ProcessHandle.current().pid();
      requestHandshake = new Document("request_type", "HANDSHAKE_REQUEST")
                          .append("host", InetAddress.getLocalHost().getHostName())
                          .append("pid", this.pid)
                          .append("local_port", this.localPort);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    Document responseHandshake = sendRequest(requestHandshake);
      this.handle = responseHandshake.getString("connection_handle");

      Map<String, Document> values = this.storage.getAllSerialized();

      Document requestSyncStorage = new Document("request_type", "SYNC_STORAGE_REQUEST")
                          .append("storage_snapshot", values);
      Document responseSyncStorage = sendRequest(requestSyncStorage);

      Map<String, Document> theirStorageSnapshot = responseSyncStorage.get("storage_snapshot", new HashMap<String, Document>());
      storage.putAllFromSerializedMap(theirStorageSnapshot);
  }

  public String getHandle(){
    return this.handle;
  }

  public String getHost() {
    return this.host;
  }

  public Long getPid() {
    return this.pid;
  }
}