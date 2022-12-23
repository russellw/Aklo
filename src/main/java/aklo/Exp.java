package aklo;

public final class Exp extends Term2 {
  public Exp(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Exp(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.EXP;
  }
}
