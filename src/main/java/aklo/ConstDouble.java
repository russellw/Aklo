package aklo;

public final class ConstDouble extends Term {
  public final double val;

  public ConstDouble(Loc loc, double val) {
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
