package aklo;

public final class Dec extends Term1 {
  public Dec(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.DEC;
  }
}
