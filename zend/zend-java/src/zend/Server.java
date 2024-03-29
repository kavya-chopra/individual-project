package zend;

import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.Document;
import zend.RequestTypes.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

  private String host;
  private int port;
  private ServerSocket serverSocket;
  private boolean mainThreadIsRunning;
  private Storage storage;
  private Connections connections;

  public Server(String host, int port, Storage storage, Connections connections) {
    this.host = host;
    this.port = port;
    this.storage = storage;
    this.connections = connections;
    this.mainThreadIsRunning = true;
  }



  public void listen() throws IOException, ZendException {
    this.serverSocket = new ServerSocket(port);
    System.out.println("Java server listening on port: " + port);
    while (mainThreadIsRunning){
      Socket clientSocket = serverSocket.accept();
      System.out.println("Accepted client: " + clientSocket.getInetAddress());
      listenToClient(clientSocket);
    }
    serverSocket.close();
  }

  public void stop(){
    this.mainThreadIsRunning = false;
  }

  private synchronized void listenToClient(Socket clientSocket) throws IOException, ZendException {
    // Receive the serialized BSON data
    DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
    int bsonDataLength = inputStream.readInt();

    byte[] bsonData = new byte[bsonDataLength];
    inputStream.readFully(bsonData);

    BasicBSONDecoder decoder = new BasicBSONDecoder();
    BSONObject bsonObject = decoder.readObject(bsonData);

    // Get response to the request by processing the request
    Document receivedDoc = new Document(bsonObject.toMap());
    System.out.println("Received from client: " + receivedDoc.toJson());
    Request request = new Request(receivedDoc);
    Document response = request.getResponse(storage, connections);

    // Write the response back to the client
    byte[] responseBytes = BsonToBinaryAdapter.toBytes(response);
    System.out.println("Sending response to client: " + response.toJson());
    outputStream.writeInt(responseBytes.length);
    outputStream.write(responseBytes);
  }


  @Override
  public void run() {
    try {
      this.listen();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ZendException e) {
      e.printStackTrace();
    }
  }
}
