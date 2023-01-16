package aklo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public final class BigRational extends Number implements Comparable<BigRational> {
  static final BigRational ZERO = new BigRational(BigInteger.ZERO);
  static final BigRational ONE = new BigRational(BigInteger.ONE);
  final BigInteger num, den;

  private BigRational(BigInteger num) {
    this.num = num;
    den = BigInteger.ONE;
  }

  private BigRational(BigInteger num, BigInteger den) {
    switch (den.signum()) {
      case -1 -> {
        num = num.negate();
        den = den.negate();
      }
      case 0 -> throw new ArithmeticException();
    }
    var g = num.gcd(den);
    num = num.divide(g);
    den = den.divide(g);
    this.num = num;
    this.den = den;
  }

  BigRational add(BigRational b) {
    return new BigRational(num.multiply(b.den).add(b.num.multiply(den)), den.multiply(b.den));
  }

  @Override
  public int compareTo(BigRational b) {
    return num.multiply(b.den).compareTo(b.num.multiply(den));
  }

  BigRational divide(BigRational b) {
    return new BigRational(num.multiply(b.den), den.multiply(b.num));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BigRational that = (BigRational) o;
    return num.equals(that.num) && den.equals(that.den);
  }

  @Override
  public int hashCode() {
    return Objects.hash(num, den);
  }

  @Override
  public double doubleValue() {
    // Potential better algorithm:
    // https://stackoverflow.com/questions/33623875/converting-an-arbitrary-precision-rational-number-ocaml-zarith-to-an-approxim
    return num.doubleValue() / den.doubleValue();
  }

  @Override
  public float floatValue() {
    return (float) doubleValue();
  }

  @Override
  public int intValue() {
    return num.divide(den).intValue();
  }

  @Override
  public long longValue() {
    return num.divide(den).longValue();
  }

  BigRational multiply(BigRational b) {
    return new BigRational(num.multiply(b.num), den.multiply(b.den));
  }

  BigRational negate() {
    return new BigRational(num.negate(), den);
  }

  static BigRational of(BigDecimal value) {
    var n = value.unscaledValue();
    var scale = value.scale();
    return (scale >= 0)
        ? of(n, BigInteger.TEN.pow(scale))
        : of(n.multiply(BigInteger.TEN.pow(-scale)));
  }

  static BigRational of(BigInteger num) {
    return new BigRational(num);
  }

  static BigRational of(double value) {
    return of(BigDecimal.valueOf(value));
  }

  static BigRational of(long num) {
    return of(BigInteger.valueOf(num));
  }

  static BigRational of(String s) {
    BigInteger num, den;
    var i = s.indexOf('/');
    if (i < 0) {
      num = new BigInteger(s);
      den = BigInteger.ONE;
    } else {
      num = new BigInteger(s.substring(0, i));
      den = new BigInteger(s.substring(i + 1));
    }
    return of(num, den);
  }

  int signum() {
    return num.signum();
  }

  static BigRational of(BigInteger num, BigInteger den) {
    return new BigRational(num, den);
  }

  static BigRational of(long num, long den) {
    return new BigRational(BigInteger.valueOf(num), BigInteger.valueOf(den));
  }

  static BigRational ofDecimal(String s) {
    return of(new BigDecimal(s));
  }

  BigRational subtract(BigRational b) {
    return new BigRational(num.multiply(b.den).subtract(b.num.multiply(den)), den.multiply(b.den));
  }

  @Override
  public String toString() {
    return num.toString() + '/' + den;
  }
}
