package aklo;

public final class FloatConst extends Term {
  public final float value;

  public FloatConst(Location location, float value) {
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
