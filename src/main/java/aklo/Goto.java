package aklo;

public final class Goto extends Term {
  public final Block target;

  @Override
  public boolean isTerminator() {
    return true;
  }

  public Goto(Loc loc, Block target) {
    super(loc);
    this.target = target;
  }

  @Override
  public Tag tag() {
    return Tag.GOTO;
  }
}
