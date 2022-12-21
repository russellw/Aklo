package aklo;

import java.math.BigInteger;

public final class Shl extends Term2 {
  public Shl(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public BigInteger apply(BigInteger a, BigInteger b) {
    return a.shiftLeft(b.intValueExact());
  }

  @Override
  public Tag tag() {
    return Tag.SHL;
  }
}
