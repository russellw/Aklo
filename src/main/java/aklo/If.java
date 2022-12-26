package aklo;

public final class If extends Term1 {
  public final Block trueTarget, falseTarget;

  @Override
  public boolean isTerminator() {
    return true;
  }

  public If(Loc loc, Term cond, Block trueTarget, Block falseTarget) {
    super(loc, cond);
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }

  @Override
  public Tag tag() {
    return Tag.IF;
  }

  @Override
  public Term remake(Loc loc, Term arg) {
    throw new UnsupportedOperationException(toString());
  }
}
