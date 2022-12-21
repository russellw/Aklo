package aklo;

import java.math.BigInteger;

public final class ConstInteger extends Term {
  public final BigInteger val;

  public ConstInteger(Loc loc, BigInteger val) {
    super(loc);
    this.val = val;
  }

  public ConstInteger(Loc loc, long val) {
    super(loc);
    this.val = BigInteger.valueOf(val);
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
    return val;
  }

  @Override
  public BigRational rationalVal() {
    return BigRational.of(val);
  }

  @Override
  public int intValExact() {
    return val.intValueExact();
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
