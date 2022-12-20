package aklo;

public final class DoubleConst extends Term {
  public final double value;

  public DoubleConst(Loc loc, double value) {
    super(loc);
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
