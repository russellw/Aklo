package aklo;

import java.util.List;

final class Var {
  String name;
  String type = "Ljava/lang/Object;";

  Var(String name, List<Var> s) {
    this.name = name;
    s.add(this);
  }

  Var(List<Var> s) {
    name = null;
    s.add(this);
  }

  @Override
  public String toString() {
    if (name != null) return name;
    return '#' + Integer.toHexString(hashCode());
  }
}
