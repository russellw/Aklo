package aklo;

public final class FloatConst extends Term {
  public final float val;

  public FloatConst(Loc loc, float val) {
    super(loc);
    this.val = val;
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
