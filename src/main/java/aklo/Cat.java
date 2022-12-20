package aklo;

public final class Cat extends Term2 {
  public Cat(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.CAT;
  }
}
