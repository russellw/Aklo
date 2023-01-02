package aklo;

import java.util.List;

public final class Do extends Terms {
  public Do(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.DO;
  }
}
