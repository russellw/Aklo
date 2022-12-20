package aklo;

import java.math.BigInteger;

public final class ConstInteger extends Term {
  public final BigInteger val;

  public ConstInteger(Loc loc, BigInteger val) {
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
