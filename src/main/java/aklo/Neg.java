package aklo;

public final class Neg extends Term1 {
  public Neg(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.NEG;
  }
}
