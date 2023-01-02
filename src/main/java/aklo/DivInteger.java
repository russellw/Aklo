package aklo;

public final class DivInteger extends Term2 {
  public DivInteger(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.DIV_INTEGER;
  }
}
