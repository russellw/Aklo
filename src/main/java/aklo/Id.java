package aklo;

public final class Id extends Term {
  public final String name;

  public Id(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.ID;
  }

  @Override
  public Type type() {
    throw new IllegalArgumentException();
  }
}
