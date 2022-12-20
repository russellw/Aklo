package aklo;

public final class Mul extends Term2 {
  public Mul(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.MUL;
  }
}
