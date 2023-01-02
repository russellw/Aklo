package aklo;

import static org.junit.Assert.*;

import org.junit.Test;

public class TermTest {
  @Test
  public void size() {
    var a = new Add(null, new ConstInteger(null, 1), new ConstInteger(null, 2));
    assertEquals(a.size(), 2);
  }
}
