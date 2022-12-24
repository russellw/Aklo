package aklo;

public final class Return extends Term1 {
  public Return(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Term remake(Loc loc, Term arg) {
    return new Return(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.RETURN;
  }
}
