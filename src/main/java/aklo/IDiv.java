package aklo;

public final class IDiv extends Term2 {
  public IDiv(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.IDIV;
  }
}
