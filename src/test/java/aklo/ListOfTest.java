package aklo;

import static org.junit.Assert.*;

import java.math.BigInteger;
import org.junit.Test;

public class ListOfTest {
  @Test
  public void of() {
    byte[] bytes = new byte[] {1, 2, 3};
    var r = ListOf.of(null, bytes);

    var a = (Const) r.get(0);
    assertEquals(a.val, BigInteger.ONE);

    a = (Const) r.get(1);
    assertEquals(a.val, BigInteger.TWO);

    bytes[0] = -1;
    r = ListOf.of(null, bytes);

    a = (Const) r.get(0);
    assertEquals(a.val, BigInteger.valueOf(255));
  }

  @Test
  public void encode() {
    var r = ListOf.encode(null, "ABC");

    var a = (Const) r.get(0);
    assertEquals(a.val, BigInteger.valueOf(65));

    a = (Const) r.get(1);
    assertEquals(a.val, BigInteger.valueOf(66));
  }
}
