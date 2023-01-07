package aklo;

import java.util.List;

public final class ListRest extends Terms {
  public ListRest(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.LIST_REST;
  }
}
