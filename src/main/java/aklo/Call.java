package aklo;

import java.util.List;

public final class Call extends Terms {
  public Call(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  public Call(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  @Override
  public Term remake(Loc loc, Term[] terms) {
    return new Call(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.CALL;
  }
}
