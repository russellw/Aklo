package aklo;

public final class Label extends Term {
  public final String name;

  public Label(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  public Tag tag() {
    return Tag.LABEL;
  }
}
