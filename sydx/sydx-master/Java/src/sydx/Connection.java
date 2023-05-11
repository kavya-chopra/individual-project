package sydx;

import java.time.LocalDateTime;

public class Connection{
  private final String host;
  private final Integer pid;
  private final int local_port;
  private final LocalDateTime date_time;
  private Client client;

  public Connection(String host, Integer pid, int local_port, LocalDateTime date_time, Client client){
    this.host = host;
    this.pid = pid;
    this.local_port = local_port;
    this.date_time = date_time;
    this.client = client;
  }

  public String getHost(){
    return this.host;
  }

  public Integer getPid(){
    return this.pid;
  }

  public int getLocal_port(){
    return this.local_port;
  }

  public LocalDateTime getDateTime(){
    return this.date_time;
  }

  public Client getClient(){
    return this.client;
  }

  public void setClient(Client client){
    this.client = client;
  }
}