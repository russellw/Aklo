package aklo;

import java.util.ArrayList;
import java.util.List;

public final class Fn extends Term {
  public String name;
  public final List<Var> params = new ArrayList<>();
  public Type rtype;
  public List<Term> body = new ArrayList<>();

  public Fn(Loc loc) {
    super(loc);
  }

  @Override
  public Tag tag() {
    return Tag.FN;
  }
}
