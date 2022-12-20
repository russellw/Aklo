package aklo;

public final class False extends Term {
  public False(Location location) {
    super(location);
  }

  @Override
  public Tag tag() {
    return Tag.FALSE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }
}
