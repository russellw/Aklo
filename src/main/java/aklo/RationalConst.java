package aklo;

public final class RationalConst extends Term {
  public final BigRational val;

  public RationalConst(Loc loc, BigRational val) {
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
