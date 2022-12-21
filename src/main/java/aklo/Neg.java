package aklo;

import java.math.BigInteger;

public final class Neg extends Term1 {
  public Neg(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public double apply(double a) {
    return -a;
  }

  @Override
  public float apply(float a) {
    return -a;
  }

  @Override
  public BigInteger apply(BigInteger a) {
    return a.negate();
  }

  @Override
  public BigRational apply(BigRational a) {
    return a.negate();
  }

  @Override
  public Tag tag() {
    return Tag.NEG;
  }
}
