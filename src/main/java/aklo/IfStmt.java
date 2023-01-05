package aklo;


public final class IfStmt extends Term3 {

  public IfStmt(Term cond, Term yes, Term no) {
    super(cond.loc, cond, yes, no);
  }

  @Override
  public Tag tag() {
    return Tag.IF;
  }
}
