package aklo;

public final class And extends Term2 {
  public And(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.AND;
  }
}
