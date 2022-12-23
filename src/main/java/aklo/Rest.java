package aklo;

public final class Rest extends Term1 {
  public Rest(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.REST;
  }

  @Override
  public Term remake(Loc loc, Term arg) {
    return new Rest(loc, arg);
  }
}
