package aklo;

import java.math.BigInteger;
import java.util.Iterator;

public abstract class Term2 extends Term {
  public Term arg0, arg1;

  public Term2(Loc loc, Term arg0, Term arg1) {
    super(loc);
    this.arg0 = arg0;
    this.arg1 = arg1;
  }

  @Override
  public Type type() {
    return arg0.type();
  }

  @Override
  public void set(int i, Term a) {
    switch (i) {
      case 0 -> arg0 = a;
      case 1 -> arg1 = a;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    }
  }

  @Override
  public Term get(int i) {
    return switch (i) {
      case 0 -> arg0;
      case 1 -> arg1;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    };
  }

  public double apply(double a, double b) {
    throw new UnsupportedOperationException(toString());
  }

  public float apply(float a, float b) {
    throw new UnsupportedOperationException(toString());
  }

  public BigInteger apply(BigInteger a, BigInteger b) {
    throw new UnsupportedOperationException(toString());
  }

  public BigRational apply(BigRational a, BigRational b) {
    throw new UnsupportedOperationException(toString());
  }

  public static Object eval(Term2 op, Object a, Object b) {
    if (a instanceof BigInteger a1) {
      // most likely case is operands of the same type
      if (b instanceof BigInteger b1) return op.apply(a1, b1);

      // floating-point numbers are more common than exact rationals
      if (b instanceof Float b1) return op.apply(a1.floatValue(), b1);
      if (b instanceof Double b1) return op.apply(a1.doubleValue(), b1);
      if (b instanceof BigRational b1) return op.apply(BigRational.of(a1), b1);

      // allow Boolean operands for convenience
      if (b instanceof Boolean b1) return op.apply(a1, b1 ? BigInteger.ONE : BigInteger.ZERO);
    } else if (a instanceof Float a1) {
      // most likely case is operands of the same type
      if (b instanceof Float b1) return op.apply(a1, b1);

      // or of different floating-point types
      if (b instanceof Double b1) return op.apply(a1, b1);

      // integers are more common than exact rationals
      if (b instanceof BigInteger b1) return op.apply(a1, b1.floatValue());
      if (b instanceof BigRational b1) return op.apply(a1, b1.floatValue());

      // allow Boolean operands for convenience
      if (b instanceof Boolean b1) return op.apply(a1, b1 ? 1.0f : 0.0f);
    } else if (a instanceof Double a1) {
      // most likely case is operands of the same type
      if (b instanceof Double b1) return op.apply(a1, b1);

      // or of different floating-point types
      if (b instanceof Float b1) return op.apply(a1, b1);

      // integers are more common than rationals
      if (b instanceof BigInteger b1) return op.apply(a1, b1.doubleValue());
      if (b instanceof BigRational b1) return op.apply(a1, b1.doubleValue());

      // allow Boolean operands for convenience
      if (b instanceof Boolean b1) return op.apply(a1, b1 ? 1.0 : 0.0);
    } else if (a instanceof BigRational a1) {
      // most likely case is operands of the same type
      if (b instanceof BigRational b1) return op.apply(a1, b1);

      // or both exact numbers
      if (b instanceof BigInteger b1) return op.apply(a1, BigRational.of(b1));

      // floating point is less likely
      if (b instanceof Float b1) return op.apply(a1.floatValue(), b1);
      if (b instanceof Double b1) return op.apply(a1.doubleValue(), b1);

      // allow Boolean operands for convenience
      if (b instanceof Boolean b1) return op.apply(a1, b1 ? BigRational.ONE : BigRational.ZERO);
    } else if (a instanceof Boolean a1) {
      // most likely case is integers
      if (b instanceof BigInteger b1) return op.apply(a1 ? BigInteger.ONE : BigInteger.ZERO, b1);
      if (b instanceof Boolean b1)
        return op.apply(
            a1 ? BigInteger.ONE : BigInteger.ZERO, b1 ? BigInteger.ONE : BigInteger.ZERO);

      // or floating-point numbers
      if (b instanceof Float b1) return op.apply(a1 ? 1.0f : 0.0f, b1);
      if (b instanceof Double b1) return op.apply(a1 ? 1.0 : 0.0, b1);

      // rationals are least likely
      if (b instanceof BigRational b1) return op.apply(a1 ? BigRational.ONE : BigRational.ZERO, b1);
    }
    throw new IllegalArgumentException(String.format("%s(%s, %s)", op, a, b));
  }

  @Override
  public int size() {
    return 2;
  }

  @Override
  public final Iterator<Term> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        assert i >= 0;
        return i < 2;
      }

      @Override
      public Term next() {
        return get(i++);
      }
    };
  }
}
