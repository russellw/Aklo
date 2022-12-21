package aklo;

public final class Rem extends Term2 {
  public Rem(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public double apply(double a, double b) {
    return a % b;
  }

  @Override
  public float apply(float a, float b) {
    return a % b;
  }

  @Override
  public BigRational apply(BigRational a, BigRational b) {
    return a.add(b);
  }

  @Override
  public Tag tag() {
    return Tag.REM;
  }
}
