package aklo;

public final class EqNumbers extends Term2 {
  public EqNumbers(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.EQ_NUMBERS;
  }
}
