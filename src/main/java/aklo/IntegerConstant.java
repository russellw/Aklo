package aklo;

import java.math.BigInteger;

public final class IntegerConstant extends Term {
  public final BigInteger value;

  public IntegerConstant(Location location, BigInteger value) {
    super(location);
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
