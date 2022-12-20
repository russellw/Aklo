package aklo;

public final class ConstFloat extends Term {
  public final float val;

  public ConstFloat(Loc loc, float val) {
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
