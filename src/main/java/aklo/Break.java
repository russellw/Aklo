package aklo;

public final class Break extends Term {
  public Break(Loc loc) {
    super(loc);
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
