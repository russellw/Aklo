package aklo;

import static org.junit.Assert.*;

import java.math.BigInteger;
import org.junit.Test;

public class TermTest {
  @Test
  public void map() {
    var a = new Add(null, new ConstInteger(null, 1), new ConstInteger(null, 2));
    var b =
        a.map(
            x -> {
              if (x instanceof ConstInteger x1)
                return new ConstInteger(x.loc, x1.integerVal().multiply(BigInteger.TEN));
              return x;
            });
    assertEquals(b.tag(), Tag.ADD);
    assertEquals(b.get(0).integerVal(), BigInteger.valueOf(10));
    assertEquals(b.get(1).integerVal(), BigInteger.valueOf(20));
  }
}
