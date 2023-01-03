package aklo;

import java.util.Map;

public final class Goto extends Term {
  public final Block target;

  @Override
  public boolean isTerminator() {
    return true;
  }

  @Override
  public void dbg(Map<Term, Integer> refs) {
    super.dbg(refs);
    System.out.print(" " + target);
  }

  public Goto(Loc loc, Block target) {
    super(loc);
    this.target = target;
  }

  @Override
  public Tag tag() {
    return Tag.GOTO;
  }
}
