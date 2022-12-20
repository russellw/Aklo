package aklo;

public final class True extends Term {
  public True(Location location) {
    super(location);
  }

  @Override
  public Tag tag() {
    return Tag.TRUE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }
}
