package aklo;

public abstract class Term {
  public final Loc loc;

  public Term(Loc loc) {
    this.loc = loc;
  }

  public abstract Tag tag();

  public abstract Type type();
}
