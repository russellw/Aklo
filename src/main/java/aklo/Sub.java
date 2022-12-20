package aklo;

public final class Sub extends Term2 {
  public Sub(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.SUB;
  }
}
