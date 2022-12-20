package aklo;

public final class FloatConstant extends Term {
  public final float value;

  public FloatConstant(Location location, float value) {
    super(location);
    this.value = value;
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
