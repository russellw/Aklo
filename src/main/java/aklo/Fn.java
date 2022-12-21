package aklo;

import java.util.ArrayList;
import java.util.List;

public final class Fn extends Term {
  public final String name;
  public final List<Var> params = new ArrayList<>();
  public Type rtype;

  public Fn(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.FN;
  }
}
