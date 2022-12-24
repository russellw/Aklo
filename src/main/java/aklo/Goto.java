package aklo;

public final class Goto extends Term {
  public final Block target;

  public Goto(Loc loc, Block target) {
    super(loc);
    this.target = target;
  }

  @Override
  public Tag tag() {
    return Tag.GOTO;
  }
}
