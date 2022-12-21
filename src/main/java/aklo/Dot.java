package aklo;

import java.util.List;

public final class Dot extends Term {
  public final String[] names;

  public Dot(Loc loc, List<String> names) {
    super(loc);
    this.names = names.toArray(new String[0]);
  }

  @Override
  public Tag tag() {
    return Tag.DOT;
  }
}
