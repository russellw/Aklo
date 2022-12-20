package aklo;

public final class DoubleConst extends Term {
  public final double val;

  public DoubleConst(Loc loc, double val) {
    super(loc);
    this.val = val;
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
