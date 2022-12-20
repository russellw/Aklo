package aklo;

public final class PostDec extends Term1 {
  public PostDec(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.POST_DEC;
  }
}
