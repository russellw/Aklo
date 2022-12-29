package aklo;

public final class Getfield extends Term1 {
  public final String owner;
  public final String name;
  public final String descriptor;

  public Getfield(Loc loc, String owner, String name, String descriptor, Term arg) {
    super(loc, arg);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public Tag tag() {
    return Tag.GETFIELD;
  }

  @Override
  public Term remake(Loc loc, Term arg) {
    return null;
  }
}
