package aklo;

public final class RationalConst extends Term {
  public final BigRational value;

  public RationalConst(Location location, BigRational value) {
    super(location);
    this.value = value;
  }

  @Override
  public Tag tag() {
    return Tag.RATIONAL;
  }

  @Override
  public Type type() {
    return Type.RATIONAL;
  }
}
