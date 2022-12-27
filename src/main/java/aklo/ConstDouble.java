package aklo;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class ConstDouble extends Term {
  public final double val;

  public ConstDouble(Loc loc, double val) {
    super(loc);
    this.val = val;
  }

  @Override
  public double doubleVal() {
    return val;
  }

  @Override
  public String toString() {
    return Double.toString(val);
  }

  @Override
  public float floatVal() {
    return (float) val;
  }

  @Override
  public BigInteger integerVal() {
    return BigDecimal.valueOf(val).toBigInteger();
  }

  @Override
  public BigRational rationalVal() {
    return BigRational.of(val);
  }

  @Override
  public Tag tag() {
    return Tag.DOUBLE;
  }

  @Override
  public Type type() {
    return Type.DOUBLE;
  }
}
