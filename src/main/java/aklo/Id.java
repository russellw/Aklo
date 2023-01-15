package aklo;

final class Id extends Term {
  final String name;

  @Override
  public String toString() {
    return name;
  }

  Id(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  @Override
  Tag tag() {
    return Tag.ID;
  }
}
