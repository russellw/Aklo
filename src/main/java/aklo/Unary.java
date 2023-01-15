package aklo;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

abstract class Unary extends Term {
  Term arg;

  Unary(Loc loc, Term arg) {
    super(loc);
    this.arg = arg;
  }

  @Override
  Type type() {
    return arg.type();
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  void set(int i, Term a) {
    assert i == 0;
    arg = a;
  }

  @Override
  Term get(int i) {
    assert i == 0;
    return arg;
  }

  static Object eval(Unary op, Object a) {
    // most likely case is integers
    if (a instanceof BigInteger a1) return op.apply(a1);

    // floating-point numbers are more common than exact rationals
    if (a instanceof Float a1) return op.apply(a1);
    if (a instanceof Double a1) return op.apply(a1);
    if (a instanceof BigRational a1) return op.apply(a1);

    // allow Boolean operands for convenience
    if (a instanceof Boolean a1) return op.apply(a1 ? BigInteger.ONE : BigInteger.ZERO);

    // extend to lists
    if (a instanceof List a1) {
      var r = new Object[a1.size()];
      for (var i = 0; i < r.length; i++) r[i] = eval(op, a1.get(i));
      return Arrays.asList(r);
    }
    throw new IllegalArgumentException(String.format("%s(%s)", op, a));
  }

  double apply(double a) {
    throw new UnsupportedOperationException(toString());
  }

  float apply(float a) {
    throw new UnsupportedOperationException(toString());
  }

  BigInteger apply(BigInteger a) {
    throw new UnsupportedOperationException(toString());
  }

  BigRational apply(BigRational a) {
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
