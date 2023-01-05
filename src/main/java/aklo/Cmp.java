package aklo;

public final class Cmp extends Term2 {
  public Cmp(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.CMP;
  }
}
