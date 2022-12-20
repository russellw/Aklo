package aklo;

public final class Continue extends Term {
  public Continue(Location location) {
    super(location);
  }

  @Override
  public Tag tag() {
    return Tag.CONTINUE;
  }

  @Override
  public Type type() {
    return Type.VOID;
  }
}
