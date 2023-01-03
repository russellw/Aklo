package aklo;

public final class Var extends Term {
  public final String name;
  public Type type = Type.ANY;
  public Object val;

  public Var(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  public Var(Loc loc) {
    super(loc);
    name = null;
  }

  @Override
  public Tag tag() {
    return Tag.VAR;
  }

  @Override
  public String toString() {
    if (name != null) return name;
    return "#" + hashCode();
  }

  @Override
  public Type type() {
    return type;
  }
}
