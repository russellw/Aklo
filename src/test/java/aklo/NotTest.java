package aklo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;

public class NotTest {
  @Test
  public void test() {
    var loc = new Loc("test", 1);
    var a = new Not(loc, new True(loc));

    assertEquals(a.tag(), Tag.NOT);
    assertEquals(a.type(), Type.BOOL);
    assertEquals(a.size(), 1);

    var r = new ArrayList<Term>(a);
    assertEquals(r.size(), 1);
    assertEquals(r.get(0).tag(), Tag.TRUE);
  }
}
