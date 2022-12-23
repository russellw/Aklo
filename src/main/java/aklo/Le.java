package aklo;

public final class Le extends Term2 {
  public Le(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Le(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.LE;
  }
}
