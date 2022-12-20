package aklo;

public final class BitOr extends Term2 {
  public BitOr(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_OR;
  }
}
