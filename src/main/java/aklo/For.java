package aklo;

import java.util.List;

public final class For extends Terms {
  public For(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  public For(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.FOR;
  }
}
