package aklo;

import java.math.BigInteger;

public final class Sub extends Term2 {
  public Sub(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public double apply(double a, double b) {
    return a - b;
  }

  @Override
  public float apply(float a, float b) {
    return a - b;
  }

  @Override
  public BigInteger apply(BigInteger a, BigInteger b) {
    return a.subtract(b);
  }

  @Override
  public BigRational apply(BigRational a, BigRational b) {
    return a.subtract(b);
  }

  @Override
  public Term remake(Loc loc, Term arg0, Term arg1) {
    return new Sub(loc, arg0, arg1);
  }

  @Override
  public Tag tag() {
    return Tag.SUB;
  }
}
