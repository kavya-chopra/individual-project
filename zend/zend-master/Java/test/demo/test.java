package demo;

import org.junit.Test;
import zend.Zend;
import zend.ZendException;

import java.util.ArrayList;
import java.util.List;

public class test {

  Zend zend = new Zend();

  public static void main(String[] args) throws ZendException {
    // Open port
    Zend.port(4784);
    // Connect to host
    Zend.connect("localhost", 4782);

    // Put name "John"
    Zend.put("name", "John");

    // Put an ArrayList {1, 2, 3}
    List numbers = new ArrayList();
    numbers.add(1);
    numbers.add(2);
    Zend.put("numbers", numbers);

    // Get name from storage
    System.out.println(Zend.get("name"));

    // Get list of numbers
    System.out.println(Zend.get("numbers"));

    // Get age '100' from other side of connection
    System.out.println(Zend.get("age"));

    // Get dict with name 'hello'
    System.out.println(Zend.get("hello"));
  }

  @Test
  public void openPort() throws ZendException {
    // Open port
    zend.port(4784);
    // Connect to host
    zend.connect("localhost", 4782);
  }

  @Test
  public void putNameAndList() {
    // Put name "John"
    zend.put("name", "John");

    // Put an ArrayList {1, 2, 3}
    List numbers = new ArrayList();
    numbers.add(1);
    numbers.add(2);
    zend.put("numbers", numbers);

    System.out.println(zend.getStorageAsString());
  }

  @Test
  public void getNameJohn() {
    // Get name from storage
    System.out.println(zend.get("name"));
  }

  @Test
  public void getNumbers() {
    System.out.println(zend.get("numbers"));
  }

  @Test
  public void getNamePaulAndDictHello() {
    System.out.println(zend.get("name"));

    System.out.println(zend.get("hello"));
  }

  @Test
  public void getAgeHundred() {
    System.out.println(zend.get("age"));
  }
}
