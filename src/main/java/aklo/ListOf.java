package aklo;

import java.util.List;

public final class ListOf extends Terms {
  public ListOf(Loc loc, List<Term> terms) {
    super(loc, terms.toArray(new Term[0]));
  }

  public ListOf(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.LIST_OF;
  }
}
