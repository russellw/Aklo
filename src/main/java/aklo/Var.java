package aklo;

import java.util.List;

public final class Var extends Term {
  public String name;
  public Type type = Type.ANY;

  public Var(String name, List<Var> s) {
    super(null);
    this.name = name;
    s.add(this);
  }

  public Var(List<Var> s) {
    super(null);
    name = null;
    s.add(this);
  }

  @Override
  public Tag tag() {
    return Tag.VAR;
  }

  @Override
  public String toString() {
    if (name != null) return name;
    return '#' + Integer.toHexString(hashCode());
  }

  @Override
  public Type type() {
    return type;
  }
}
