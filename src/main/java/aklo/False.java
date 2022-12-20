package aklo;

public final class False extends Term {
  public False(Loc loc) {
    super(loc);
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
