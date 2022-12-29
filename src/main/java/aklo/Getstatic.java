package aklo;

public final class Getstatic extends Term {
  public final String owner;
  public final String name;
  public final String descriptor;

  public Getstatic(Loc loc, String owner, String name, String descriptor) {
    super(loc);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public Tag tag() {
    return Tag.GETSTATIC;
  }
}
