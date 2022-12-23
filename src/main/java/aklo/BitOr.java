package aklo;

import java.math.BigInteger;

public final class BitOr extends Term2 {
  public BitOr(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new BitOr(loc, arg0, arg1);
  }

  @Override
  public BigInteger apply(BigInteger a, BigInteger b) {
    return a.or(b);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_OR;
  }
}
