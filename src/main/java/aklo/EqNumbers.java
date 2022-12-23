package aklo;

public final class EqNumbers extends Term2 {
  public EqNumbers(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new EqNumbers(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.EQ_NUMBERS;
  }
}
