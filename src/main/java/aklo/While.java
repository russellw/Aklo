package aklo;

import java.util.List;

public final class While extends Terms {
  public String label;
  public final boolean doWhile;

  public While(Loc loc, boolean doWhile, List<Term> terms) {
    super(loc, terms);
    this.doWhile = doWhile;
  }

  public While(Loc loc, String label, boolean doWhile, Term[] terms) {
    super(loc, terms);
    this.label = label;
    this.doWhile = doWhile;
  }

  @Override
  public Tag tag() {
    return Tag.WHILE;
  }
}
