package aklo;

import java.math.BigInteger;
import java.util.Iterator;

public abstract class Term1 extends Term {
  public Term arg;

  public Term1(Loc loc, Term arg) {
    super(loc);
    this.arg = arg;
  }

  @Override
  public Type type() {
    return arg.type();
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void set(int i, Term a) {
    assert i == 0;
    arg = a;
  }

  @Override
  public Term get(int i) {
    assert i == 0;
    return arg;
  }

  public static Object eval(Term1 op, Object a) {
    // most likely case is integers
    if (a instanceof BigInteger a1) return op.apply(a1);

    // floating-point numbers are more common than exact rationals
    if (a instanceof Float a1) return op.apply(a1);
    if (a instanceof Double a1) return op.apply(a1);
    if (a instanceof BigRational a1) return op.apply(a1);

    // allow Boolean operands for convenience
    if (a instanceof Boolean a1) return op.apply(a1 ? BigInteger.ONE : BigInteger.ZERO);
    throw new IllegalArgumentException(String.format("%s(%s)", op, a));
  }

  public double apply(double a) {
    throw new UnsupportedOperationException(toString());
  }

  public float apply(float a) {
    throw new UnsupportedOperationException(toString());
  }

  public BigInteger apply(BigInteger a) {
    throw new UnsupportedOperationException(toString());
  }

  public BigRational apply(BigRational a) {
    throw new UnsupportedOperationException(toString());
  }

  @Override
  public final Iterator<Term> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        return i == 0;
      }

      @Override
      public Term next() {
        assert i == 0;
        i++;
        return arg;
      }
    };
  }
}
