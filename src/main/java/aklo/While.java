package aklo;

public final class While extends Term2 {
  public String label;
  public final boolean doWhile;

  public While(boolean doWhile, Term cond, Term body) {
    super(cond.loc, cond, body);
    this.doWhile = doWhile;
  }

  @Override
  public Tag tag() {
    return Tag.WHILE;
  }
}
