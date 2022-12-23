package aklo;

public final class Throw extends Term1 {
  @Override
  public Term remake(Loc loc, Term arg) {
    return new Throw(loc, arg);
  }

  public Throw(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.THROW;
  }
}
