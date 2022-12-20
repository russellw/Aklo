package aklo;

import java.math.BigInteger;

public final class IntegerConst extends Term {
  public final BigInteger value;

  public IntegerConst(Loc loc, BigInteger value) {
    super(loc);
    this.value = value;
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
