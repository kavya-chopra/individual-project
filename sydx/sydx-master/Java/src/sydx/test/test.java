package sydx.test;

import sydx.Sydx;
import sydx.SydxException;

public class test {
  public static void main(String[] args) throws SydxException {
    Sydx.port(4784);
    System.out.println("Opened port");

    String handle = Sydx.connect("", 4782);
    System.out.println("Connected. Handle: " + handle);

    Sydx.put("name", "John");
    Sydx.put("number", 1);

    System.out.println(Sydx.get("name"));
    System.out.println(Sydx.get("number"));

    System.out.println(Sydx.get("age"));
  }
}
