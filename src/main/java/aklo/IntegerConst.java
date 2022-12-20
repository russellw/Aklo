package aklo;

import java.math.BigInteger;

public final class IntegerConst extends Term {
  public final BigInteger val;

  public IntegerConst(Loc loc, BigInteger val) {
    super(loc);
    this.val = val;
  }

  @Override
  public Tag tag() {
    return Tag.INTEGER;
  }

  @Override
  public Type type() {
    return Type.INTEGER;
  }
}
