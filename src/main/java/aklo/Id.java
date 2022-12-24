package aklo;

public final class Id extends Term {
  public final String string;

  public Id(Loc loc, String string) {
    super(loc);
    this.string = string;
  }

  @Override
  public Tag tag() {
    return Tag.ID;
  }
}
