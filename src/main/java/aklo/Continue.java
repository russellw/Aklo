package aklo;

public final class Continue extends Term {
  public final String name;

  public Continue(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.CONTINUE;
  }
}
