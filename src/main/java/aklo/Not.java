package aklo;

public final class Not extends Term1 {
  public Not(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.NOT;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }
}
