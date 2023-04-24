package sydx;

public class SydxException extends Exception {

  public SydxException(){
    super();
  }

  public SydxException(String error) {
    System.out.println(error);
  }
}