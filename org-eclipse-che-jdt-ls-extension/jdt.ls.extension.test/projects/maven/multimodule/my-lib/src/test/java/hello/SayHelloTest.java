package hello;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Unit test for simple App. */
public class SayHelloTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public SayHelloTest(String testName) {
    super(testName);
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(SayHelloTest.class);
  }

  /** Rigourous Test :-) */
  public void testSayHello() {
    SayHello sayHello = new SayHello();
    assertTrue("Hello, codenvy".equals(sayHello.sayHello("codenvy")));
  }
}
