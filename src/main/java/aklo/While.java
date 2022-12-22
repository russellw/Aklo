package aklo;

import java.util.List;

public final class While extends Terms {
  public final boolean doWhile;

  public While(Loc loc, boolean doWhile, List<Term> terms) {
    super(loc, terms);
    this.doWhile = doWhile;
  }

  @Override
  public Tag tag() {
    return Tag.WHILE;
  }
}
