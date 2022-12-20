package aklo;

public final class Add extends Term2 {
  public Add(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.ADD;
  }
}
