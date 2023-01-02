package aklo;

public final class PostInc extends Term1 {
  public final int inc;

  public PostInc(Loc loc, Term arg, int inc) {
    super(loc, arg);
    this.inc = inc;
  }

  @Override
  public Tag tag() {
    return Tag.POST_INC;
  }
}
