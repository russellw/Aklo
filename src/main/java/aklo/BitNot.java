package aklo;

public final class BitNot extends Term1 {
  public BitNot(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_NOT;
  }
}
