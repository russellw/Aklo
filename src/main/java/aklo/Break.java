package aklo;

public final class Break extends Term {
  public Break(Location location) {
    super(location);
  }

  @Override
  public Tag tag() {
    return Tag.BREAK;
  }

  @Override
  public Type type() {
    return Type.VOID;
  }
}
