package aklo;

public final class Def extends Term2 {
  public Def(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.DEF;
  }
}
