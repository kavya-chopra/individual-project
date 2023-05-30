package zend;

import java.time.LocalDateTime;

public class Connection{
  private final String host;
  private final Integer pid;
  private final int localPort;
  private final LocalDateTime dateTime;
  private Client client;

  public Connection(String host, Integer pid, int localPort, LocalDateTime dateTime, Client client){
    this.host = host;
    this.pid = pid;
    this.localPort = localPort;
    this.dateTime = dateTime;
    this.client = client;
  }

  public String getHost(){
    return this.host;
  }

  public Integer getPid(){
    return this.pid;
  }

  public int getLocalPort(){
    return this.localPort;
  }

  public LocalDateTime getDateTime(){
    return this.dateTime;
  }

  public Client getClient(){
    return this.client;
  }

  public void setClient(Client client){
    this.client = client;
  }
}