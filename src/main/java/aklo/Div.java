package aklo;

public final class Div extends Term2 {
  public Div(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Div(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.DIV;
  }
}
