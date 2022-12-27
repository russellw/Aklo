package aklo;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class ConstFloat extends Term {
  public final float val;

  public ConstFloat(Loc loc, float val) {
    super(loc);
    this.val = val;
  }

  @Override
  public String toString() {
    return Float.toString(val);
  }

  @Override
  public double doubleVal() {
    return val;
  }

  @Override
  public float floatVal() {
    return val;
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
    return Tag.FLOAT;
  }

  @Override
  public Type type() {
    return Type.FLOAT;
  }
}
