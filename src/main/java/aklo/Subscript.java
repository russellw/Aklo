package aklo;

public final class Subscript extends Term2 {
  public Subscript(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Subscript(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.SUBSCRIPT;
  }
}
