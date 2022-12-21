package aklo;

import java.math.BigInteger;

public final class False extends Term {
  public False(Loc loc) {
    super(loc);
  }

  @Override
  public double doubleVal() {
    return 0.0;
  }

  @Override
  public float floatVal() {
    return 0.0f;
  }

  @Override
  public BigInteger integerVal() {
    return BigInteger.ZERO;
  }

  @Override
  public BigRational rationalVal() {
    return BigRational.ZERO;
  }

  @Override
  public Tag tag() {
    return Tag.FALSE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }
}
