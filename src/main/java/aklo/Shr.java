package aklo;

import java.math.BigInteger;

public final class Shr extends Term2 {
  public Shr(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Object apply(BigInteger a, BigInteger b) {
    return a.shiftRight(b.intValueExact());
  }

  @Override
  public Tag tag() {
    return Tag.SHR;
  }
}
