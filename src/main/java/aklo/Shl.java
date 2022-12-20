package aklo;

public final class Shl extends Term2 {
  public Shl(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.SHL;
  }
}
