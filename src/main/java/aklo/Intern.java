package aklo;

public final class Intern extends Term1 {
  public Intern(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.INTERN;
  }
}
