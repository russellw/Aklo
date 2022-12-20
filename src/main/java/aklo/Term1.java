package aklo;

public abstract class Term1 extends Term {
  public final Term arg;

  public Term1(Loc loc, Term arg) {
    super(loc);
    this.arg = arg;
  }

  @Override
  public Type type() {
    return arg.type();
  }
}
