package aklo;

public final class Intern extends Term1 {
  public Intern(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public Tag tag() {
    return Tag.INTERN;
  }

  @Override
  public Term remake(Loc loc, Term arg) {
    return new Intern(loc, arg);
  }
}
