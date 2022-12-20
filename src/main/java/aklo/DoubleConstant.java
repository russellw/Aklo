package aklo;

public final class DoubleConstant extends Term {
  public final double value;

  public DoubleConstant(Location location, double value) {
    super(location);
    this.value = value;
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
