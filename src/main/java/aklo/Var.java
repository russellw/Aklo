package aklo;

public final class Var extends Term {
  public String name;
  public Type type = Type.ANY;

  public Var(String name) {
    super(null);
    this.name = name;
  }

  public Var(Fn f) {
    super(null);
    name = null;
    f.vars.add(this);
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
