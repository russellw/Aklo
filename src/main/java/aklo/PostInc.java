package aklo;

public final class PostInc extends Term1 {
  public PostInc(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.POST_INC;
  }
}
