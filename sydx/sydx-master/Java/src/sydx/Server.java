package sydx;

import sydx.RequestTypes.Interpreter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  private String host;
  private int port;
  private Interpreter interpreter;
  private ServerSocket serverSocket;
  private boolean mainThreadIsRunning;

  public Server(String host, int port, Interpreter interpreter) {
    this.host = host;
    this.port = port;
    this.interpreter = interpreter;
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

  private synchronized void listenToClient(Socket clientSocket) {
  }


}
