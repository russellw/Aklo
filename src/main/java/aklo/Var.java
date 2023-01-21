package aklo;

import java.util.List;

final class Var extends Named {
  String type = "Ljava/lang/Object;";

  Var(String name, List<Var> s) {
    super(name);
    s.add(this);
  }
}
