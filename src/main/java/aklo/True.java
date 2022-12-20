package aklo;

public final class True extends Term {
  public True(Loc loc) {
    super(loc);
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
