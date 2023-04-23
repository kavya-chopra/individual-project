package sydx;

import org.bson.Document;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import sydx.RequestTypes.Request;

public class Server {

  private String host;
  private int port;
  private sydx.RequestTypes.Request request;
  private ServerSocket serverSocket;
  private boolean mainThreadIsRunning;

  public Server(String host, int port, sydx.RequestTypes.Request request) {
    this.host = host;
    this.port = port;
    this.request = request;
    this.mainThreadIsRunning = true;
  }

  public void listen() throws IOException {
    this.serverSocket = new ServerSocket(port);
    System.out.println("Java server listening on port: "+port);
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

  private synchronized void listenToClient(Socket clientSocket) throws IOException {
    // Receive the serialized BSON data
    DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
    int bsonDataLength = inputStream.readInt();

    byte[] bsonData = new byte[bsonDataLength];
    inputStream.readFully(bsonData);

    Document receivedDoc = Document.parse(inputStream.readUTF());
    // Get response to the request by processing the request
    Request request = new Request(receivedDoc, outputStream);
    Document response = request.getResponse();

    // Write the response back to the client
    byte[] responseBytes = response.toJson().getBytes();
    outputStream.writeInt(responseBytes.length);
    outputStream.write(response.toJson().getBytes());
    clientSocket.close();
  }


}
