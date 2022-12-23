package aklo;

public final class Lt extends Term2 {
  public Lt(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Lt(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.LT;
  }
}
