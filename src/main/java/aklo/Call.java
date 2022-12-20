package aklo;

import java.util.List;

public final class Call extends Terms {
  public Call(Loc loc, List<Term> terms) {
    super(loc, terms.toArray(new Term[0]));
  }

  public Call(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.CALL;
  }

  @Override
  public Type type() {
    // TODO
    throw new IllegalArgumentException();
  }
}
