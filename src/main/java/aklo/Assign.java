package aklo;

public final class Assign extends Term2 {
  public Assign(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.ASSIGN;
  }
}
