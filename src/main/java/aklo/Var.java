package aklo;

import java.util.List;

final class Var extends Named {
  Type type = Type.OBJECT;

  Var(String name, List<Var> s) {
    super(name);
    s.add(this);
  }
}
