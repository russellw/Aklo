package aklo;

import java.util.List;

public final class IfStmt extends Terms {
  public final int then;

  @Override
  public Term remake(Loc loc, Term[] terms) {
    return new IfStmt(loc, terms, then);
  }

  public IfStmt(Loc loc, List<Term> terms, int then) {
    super(loc, terms);
    this.then = then;
  }

  public IfStmt(Loc loc, Term[] terms, int then) {
    super(loc, terms);
    this.then = then;
  }

  @Override
  public Tag tag() {
    return Tag.IF_STMT;
  }
}
