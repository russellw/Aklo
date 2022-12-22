package aklo;

public final class Var extends Term {
  public final String name;
  public Type type;
  public Term val;

  public Var(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.VAR;
  }

  @Override
  public Type type() {
    return type;
  }
}
