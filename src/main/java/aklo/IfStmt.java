package aklo;

import java.math.BigInteger;

public final class IfStmt extends Term3 {

  public IfStmt(Term cond, Term yes, Term no) {
    super(cond.loc, cond, yes, no != null ? no : new Const(cond.loc, BigInteger.ZERO));
  }

  @Override
  public Tag tag() {
    return Tag.IF;
  }
}
