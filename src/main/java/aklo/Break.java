package aklo;

public final class Break extends Term {
  public final String label;

  public Break(Loc loc, String label) {
    super(loc);
    this.label = label;
  }

  @Override
  public Tag tag() {
    return Tag.BREAK;
  }
}
