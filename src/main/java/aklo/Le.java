package aklo;

public final class Le extends Term2 {
  public Le(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.LE;
  }
}
