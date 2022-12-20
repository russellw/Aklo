package aklo;

public abstract class Term {
  public final Location location;
  public final Tag tag;

  protected Term(Location location, Tag tag) {
    this.location = location;
    this.tag = tag;
  }
}
