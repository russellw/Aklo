package aklo;

public final class Def extends Term2 {
  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Def(loc, arg0, arg1);
  }

  public Def(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.DEF;
  }
}
