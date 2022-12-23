package aklo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Fn extends Term {
  public String name;
  public final List<Var> params = new ArrayList<>();
  public Type rtype;
  public final List<Term> body = new ArrayList<>();

  public Fn(Loc loc) {
    super(loc);
  }

  public void walkFns(Consumer<Fn> f) {
    f.accept(this);
    for (var a : body)
      a.walk(
          b -> {
            if (b instanceof Fn b1) b1.walkFns(f);
          });
  }

  @Override
  public Tag tag() {
    return Tag.FN;
  }
}
