package aklo;

public final class ConstRational extends Term {
  public final BigRational val;

  public ConstRational(Loc loc, BigRational val) {
    super(loc);
    this.val = val;
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
