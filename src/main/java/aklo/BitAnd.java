package aklo;

import java.math.BigInteger;

public final class BitAnd extends Term2 {
  public BitAnd(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public BigInteger apply(BigInteger a, BigInteger b) {
    return a.and(b);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_AND;
  }
}
