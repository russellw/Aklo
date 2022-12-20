package aklo;

public final class Rem extends Term2 {
  public Rem(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.REM;
  }
}
