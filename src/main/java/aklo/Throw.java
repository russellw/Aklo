package aklo;

public final class Throw extends Term1 {
  public Throw(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.THROW;
  }
}
