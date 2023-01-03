package aklo;

import java.util.List;

public final class Case extends Terms {
  public Case(List<Term> terms) {
    super(terms.get(0).loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.CASE;
  }
}
