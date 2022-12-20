package aklo;

public abstract class Term {
  public final Location location;

  public Term(Location location) {
    this.location = location;
  }

  public abstract Tag tag();

  public abstract Type type();
}
