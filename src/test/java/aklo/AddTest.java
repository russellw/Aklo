package aklo;

import static org.junit.Assert.*;

import java.math.BigInteger;
import org.junit.Test;

public class AddTest {
  @Test
  public void apply() {
    assertEquals(new Add(null, new True(null), new True(null)).eval().integerVal(), BigInteger.TWO);
    assertEquals(
        new Add(null, new True(null), new ConstDouble(null, 50.0)).eval().integerVal(),
        BigInteger.valueOf(51));
  }
}
