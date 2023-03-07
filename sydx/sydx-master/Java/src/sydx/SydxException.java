package sydx;

public class SydxException extends Exception {
  String error;

  SydxException(){
    super();
  }

  SydxException(String error) {
    super(error);
  }
}