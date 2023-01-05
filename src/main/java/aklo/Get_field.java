package aklo;

public final class Get_field extends Term1 {
  public final String owner;
  public final String name;
  public final String descriptor;

  public Get_field(Loc loc, String owner, String name, String descriptor, Term arg) {
    super(loc, arg);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public Tag tag() {
    return Tag.GET_FIELD;
  }
}
