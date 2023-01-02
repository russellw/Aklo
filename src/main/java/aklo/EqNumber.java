package aklo;

public final class EqNumber extends Term2 {
  public EqNumber(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.EQ_NUMBER;
  }
}
