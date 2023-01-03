package aklo;

public final class Var extends Term {
  public String name;
  public Type type = Type.ANY;
  public Object val;

  public Var(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.VAR;
  }

  @Override
  public String toString() {
    assert name != null;
    return name;
  }

  @Override
  public Type type() {
    return type;
  }
}
