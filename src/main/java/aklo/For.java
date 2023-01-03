package aklo;

public final class For extends Term3 {
  public For(Term x, Term s, Term body) {
    super(x.loc, x, s, body);
  }

  @Override
  public Tag tag() {
    return Tag.FOR;
  }
}
