package aklo;

import java.math.BigInteger;

public final class ConstRational extends Term {
  public final BigRational val;

  public ConstRational(Loc loc, BigRational val) {
    super(loc);
    this.val = val;
  }

  @Override
  public double doubleVal() {
    return val.doubleValue();
  }

  @Override
  public float floatVal() {
    return val.floatValue();
  }

  @Override
  public BigInteger integerVal() {
    return val.num.divide(val.den);
  }

  @Override
  public BigRational rationalVal() {
    return val;
  }

  @Override
  public Tag tag() {
    return Tag.RATIONAL;
  }

  @Override
  public String toString() {
    return val.toString();
  }

  @Override
  public Type type() {
    return Type.RATIONAL;
  }
}
