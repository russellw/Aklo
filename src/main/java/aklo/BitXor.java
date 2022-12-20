package aklo;

public final class BitXor extends Term2 {
  public BitXor(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_XOR;
  }
}
