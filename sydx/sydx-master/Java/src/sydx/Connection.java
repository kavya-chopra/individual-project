package sydx;

public class Connection{
  private final Object host;
  private final Object pid;
  private final int local_port;
  private final String date_time;
  private Client client;

  public Connection(Object host, Object pid, int local_port, String date_time, Client client){
    this.host = host;
    this.pid = pid;
    this.local_port = local_port;
    this.date_time = date_time;
    this.client = client;
  }

  public Object getHost(){
    return this.host;
  }

  public Object getPid(){
    return this.pid;
  }

  public int getLocal_port(){
    return this.local_port;
  }

  public String getDate_time(){
    return this.date_time;
  }

  public Client getClient(){
    return this.client;
  }

  public void setClient(Client client){
    this.client = client;
  }
}