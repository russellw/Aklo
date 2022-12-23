package aklo;

public final class Cat extends Term2 {
  public Cat(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Cat(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.CAT;
  }
}
