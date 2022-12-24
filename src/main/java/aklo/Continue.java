package aklo;

public final class Continue extends Term {
  public final String label;

  public Continue(Loc loc, String label) {
    super(loc);
    this.label = label;
  }

  @Override
  public Tag tag() {
    return Tag.CONTINUE;
  }
}
