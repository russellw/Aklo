package aklo;

import java.math.BigInteger;

public final class True extends Term {
  public True(Loc loc) {
    super(loc);
  }

  @Override
  public double doubleVal() {
    return 1.0;
  }

  @Override
  public float floatVal() {
    return 1.0f;
  }

  @Override
  public BigInteger integerVal() {
    return BigInteger.ONE;
  }

  @Override
  public BigRational rationalVal() {
    return BigRational.ONE;
  }

  @Override
  public Tag tag() {
    return Tag.TRUE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }
}
