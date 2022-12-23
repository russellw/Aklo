package aklo;

import java.util.List;

public final class If extends Terms {
  public final int then;

  @Override
  public Term remake(Loc loc, Term[] terms) {
    return new If(loc, terms, then);
  }

  public If(Loc loc, List<Term> terms, int then) {
    super(loc, terms);
    this.then = then;
  }

  public If(Loc loc, Term[] terms, int then) {
    super(loc, terms);
    this.then = then;
  }

  @Override
  public Tag tag() {
    return Tag.IF;
  }
}
