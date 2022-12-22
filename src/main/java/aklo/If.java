package aklo;

import java.util.List;

public final class If extends Terms {
  public final int then;

  public If(Loc loc, List<Term> terms, int then) {
    super(loc, terms);
    this.then = then;
  }

  @Override
  public Tag tag() {
    return Tag.IF;
  }
}
