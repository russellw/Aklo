package aklo;

import java.util.List;

final class Var extends Term {
  String name;
  Type type = Type.ANY;

  Var(String name, List<Var> s) {
    super(null);
    this.name = name;
    s.add(this);
  }

  Var(List<Var> s) {
    super(null);
    name = null;
    s.add(this);
  }

  @Override
  Tag tag() {
    return Tag.VAR;
  }

  @Override
  public String toString() {
    if (name != null) return name;
    return '#' + Integer.toHexString(hashCode());
  }

  @Override
  Type type() {
    return type;
  }
}
