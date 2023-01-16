package aklo;

import java.util.List;

final class Var {
  String name;
  String type = "Ljava/lang/Object;";

  Var(String name, List<Var> s) {
    this.name = name;
    s.add(this);
  }

  @Override
  public String toString() {
    return name;
  }
}
