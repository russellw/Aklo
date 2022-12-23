package aklo;

public final class DivIntegers extends Term2 {
  public DivIntegers(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new DivIntegers(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.DIV_INTEGERS;
  }
}
