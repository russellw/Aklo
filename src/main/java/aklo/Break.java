package aklo;

public final class Break extends Term {
  public final String name;

  public Break(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.BREAK;
  }
}
