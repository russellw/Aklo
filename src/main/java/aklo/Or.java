package aklo;

public final class Or extends Term2 {
  public Or(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Or(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.OR;
  }
}
