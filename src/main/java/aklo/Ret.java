package aklo;

public final class Ret extends Term1 {
  @Override
  public Type type() {
    return Type.VOID;
  }

  public Ret(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Term remake(Loc loc, Term arg) {
    return new Ret(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.RET;
  }
}
