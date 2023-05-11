package demo;

import org.junit.Test;
import sydx.Sydx;
import sydx.SydxException;

import java.util.ArrayList;
import java.util.List;

public class test {

  Sydx sydx = new Sydx();

  public static void main(String[] args) throws SydxException {
    // Open port
    Sydx.port(4784);
    // Connect to host
    Sydx.connect("localhost", 4782);

    // Put name "John"
    Sydx.put("name", "John");

    // Put an ArrayList {1, 2, 3}
    List numbers = new ArrayList();
    numbers.add(1);
    numbers.add(2);
    Sydx.put("numbers", numbers);

    // Get name from storage
    System.out.println(Sydx.get("name"));

    // Get list of numbers
    System.out.println(Sydx.get("numbers"));

    // Get age '100' from other side of connection
    System.out.println(Sydx.get("age"));

    // Get dict with name 'hello'
    System.out.println(Sydx.get("hello"));
  }

  @Test
  public void openPort() throws SydxException {
    // Open port
    sydx.port(4784);
    // Connect to host
    sydx.connect("localhost", 4782);
  }

  @Test
  public void putNameAndList() {
    // Put name "John"
    sydx.put("name", "John");

    // Put an ArrayList {1, 2, 3}
    List numbers = new ArrayList();
    numbers.add(1);
    numbers.add(2);
    sydx.put("numbers", numbers);

    System.out.println(sydx.getStorageAsString());
  }

  @Test
  public void getNameJohn() {
    // Get name from storage
    System.out.println(sydx.get("name"));
  }

  @Test
  public void getNumbers() {
    System.out.println(sydx.get("numbers"));
  }

  @Test
  public void getNamePaulAndDictHello() {
    System.out.println(sydx.get("name"));

    System.out.println(sydx.get("hello"));
  }

  @Test
  public void getAgeHundred() {
    System.out.println(sydx.get("age"));
  }
}
