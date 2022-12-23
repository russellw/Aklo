package aklo;

public final class Eq extends Term2 {
  public Eq(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Eq(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.EQ;
  }
}
