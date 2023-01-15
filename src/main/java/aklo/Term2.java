package aklo;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
abstract class Term2 extends Term {
  Term arg0, arg1;

  Term2(Loc loc, Term arg0, Term arg1) {
    super(loc);
    this.arg0 = arg0;
    this.arg1 = arg1;
  }

  @Override
  Type type() {
    return arg0.type();
  }

  @Override
  void set(int i, Term a) {
    switch (i) {
      case 0 -> arg0 = a;
      case 1 -> arg1 = a;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    }
  }

  @Override
  Term get(int i) {
    return switch (i) {
      case 0 -> arg0;
      case 1 -> arg1;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    };
  }

  Object apply(double a, double b) {
    throw new UnsupportedOperationException(toString());
  }

  Object apply(float a, float b) {
    throw new UnsupportedOperationException(toString());
  }

  Object apply(BigInteger a, BigInteger b) {
    throw new UnsupportedOperationException(toString());
  }

  Object apply(BigRational a, BigRational b) {
    throw new UnsupportedOperationException(toString());
  }

  static Object evals(Term2 op, List<Object> s, List<Object> t) {
    var r = new Object[s.size()];
    for (var i = 0; i < r.length; i++) r[i] = eval(op, s.get(i), t.get(i));
    return Arrays.asList(r);
  }

  @SuppressWarnings("ConstantConditions")
  static Object eval(Term2 op, Object a, Object b) {
    // atoms
    do {
      if (a instanceof BigInteger a1) {
        // most likely case is operands of the same type
        if (b instanceof BigInteger b1) return op.apply(a1, b1);

        // floating-point numbers are more common than exact rationals
        if (b instanceof Float b1) return op.apply(a1.floatValue(), b1);
        if (b instanceof Double b1) return op.apply(a1.doubleValue(), b1);
        if (b instanceof BigRational b1) return op.apply(BigRational.of(a1), b1);

        // allow Boolean operands for convenience
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? BigInteger.ONE : BigInteger.ZERO);
        break;
      }
      if (a instanceof Float a1) {
        // most likely case is operands of the same type
        if (b instanceof Float b1) return op.apply(a1, b1);

        // or of different floating-point types
        if (b instanceof Double b1) return op.apply(a1, b1);

        // integers are more common than exact rationals
        if (b instanceof BigInteger b1) return op.apply(a1, b1.floatValue());
        if (b instanceof BigRational b1) return op.apply(a1, b1.floatValue());

        // allow Boolean operands for convenience
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? 1.0f : 0.0f);
        break;
      }
      if (a instanceof Double a1) {
        // most likely case is operands of the same type
        if (b instanceof Double b1) return op.apply(a1, b1);

        // or of different floating-point types
        if (b instanceof Float b1) return op.apply(a1, b1);

        // integers are more common than rationals
        if (b instanceof BigInteger b1) return op.apply(a1, b1.doubleValue());
        if (b instanceof BigRational b1) return op.apply(a1, b1.doubleValue());

        // allow Boolean operands for convenience
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? 1.0 : 0.0);
        break;
      }
      if (a instanceof BigRational a1) {
        // most likely case is operands of the same type
        if (b instanceof BigRational b1) return op.apply(a1, b1);

        // or both exact numbers
        if (b instanceof BigInteger b1) return op.apply(a1, BigRational.of(b1));

        // floating point is less likely
        if (b instanceof Float b1) return op.apply(a1.floatValue(), b1);
        if (b instanceof Double b1) return op.apply(a1.doubleValue(), b1);

        // allow Boolean operands for convenience
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? BigRational.ONE : BigRational.ZERO);
        break;
      }
      if (a instanceof Boolean a1) {
        // most likely case is integers
        if (b instanceof BigInteger b1) return op.apply(a1 ? BigInteger.ONE : BigInteger.ZERO, b1);
        if (b instanceof Boolean b1)
          return op.apply(
              a1 ? BigInteger.ONE : BigInteger.ZERO, b1 ? BigInteger.ONE : BigInteger.ZERO);

        // or floating-point numbers
        if (b instanceof Float b1) return op.apply(a1 ? 1.0f : 0.0f, b1);
        if (b instanceof Double b1) return op.apply(a1 ? 1.0 : 0.0, b1);

        // rationals are least likely
        if (b instanceof BigRational b1)
          return op.apply(a1 ? BigRational.ONE : BigRational.ZERO, b1);
        break;
      }
    } while (false);

    // lists
    do {
      if (a instanceof List a1) {
        List<Object> b1;
        if (b instanceof List) {
          b1 = (List) b;
          if (a1.size() != b1.size()) break;
        } else b1 = Collections.nCopies(a1.size(), b);
        return evals(op, a1, b1);
      }
      if (b instanceof List b1) {
        var a1 = Collections.nCopies(b1.size(), a);
        return evals(op, a1, b1);
      }
    } while (false);

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
