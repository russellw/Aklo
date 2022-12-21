package aklo;

import static org.junit.Assert.*;

import java.math.BigInteger;
import org.junit.Test;

public class NegTest {
  @Test
  public void apply() {
    assertEquals(new Neg(null, new True(null)).eval().integerVal(), BigInteger.valueOf(-1));
    assertEquals(
        new Neg(null, new ConstDouble(null, 50.0)).eval().integerVal(), BigInteger.valueOf(-50));
  }
}
