package aklo;

public final class Inc extends Term1 {
  public Inc(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.INC;
  }
}
