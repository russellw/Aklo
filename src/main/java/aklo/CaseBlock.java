package aklo;

import java.util.List;

public final class CaseBlock extends Terms {
  public final int cases;

  public CaseBlock(Loc loc, Term[] terms, int cases) {
    super(loc, terms);
    this.cases = cases;
  }

  public CaseBlock(Loc loc, List<Term> terms, int cases) {
    super(loc, terms);
    this.cases = cases;
  }

  @Override
  public Tag tag() {
    throw new UnsupportedOperationException(toString());
  }
}
