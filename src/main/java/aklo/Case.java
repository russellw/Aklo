package aklo;

import java.util.List;

public final class Case extends Terms {
  public Case(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  public Case(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.CASE;
  }
}
