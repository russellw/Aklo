package aklo;

import java.math.BigInteger;

public final class BitNot extends Term1 {
  public BitNot(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public BigInteger apply(BigInteger a) {
    return a.not();
  }

  @Override
  public Tag tag() {
    return Tag.BIT_NOT;
  }
}
