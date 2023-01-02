package aklo;

import java.math.BigInteger;

public final class Mul extends Term2 {
  public Mul(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public double apply(double a, double b) {
    return a * b;
  }

  @Override
  public float apply(float a, float b) {
    return a * b;
  }

  @Override
  public BigInteger apply(BigInteger a, BigInteger b) {
    return a.multiply(b);
  }

  @Override
  public BigRational apply(BigRational a, BigRational b) {
    return a.multiply(b);
  }

  @Override
  public Tag tag() {
    return Tag.MUL;
  }
}
