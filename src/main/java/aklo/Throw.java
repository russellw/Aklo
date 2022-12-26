package aklo;

public final class Throw extends Term1 {
  @Override
  public Term remake(Loc loc, Term arg) {
    return new Throw(loc, arg);
  }

  @Override
  public boolean isTerminator() {
    return true;
  }

  public Throw(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.THROW;
  }
}
