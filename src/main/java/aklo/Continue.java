package aklo;

public final class Continue extends Term {
  public Continue(Loc loc) {
    super(loc);
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
