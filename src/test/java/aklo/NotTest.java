package aklo;

import static org.junit.Assert.*;

import org.junit.Test;

public class NotTest {
  @Test
  public void tag() {
    var loc = new Loc("test", 1);
    var a = new Not(loc, new True(loc));
    assertEquals(a.tag(), Tag.NOT);
  }

  @Test
  public void type() {
    var loc = new Loc("test", 1);
    var a = new Not(loc, new True(loc));
    assertEquals(a.type(), Type.BOOL);
  }
}
