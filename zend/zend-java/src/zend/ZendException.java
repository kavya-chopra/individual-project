package zend;

public class ZendException extends Exception {

  public ZendException(){
    super();
  }

  public ZendException(String error) {
    System.out.println(error);
  }
}