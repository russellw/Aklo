package aklo;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
abstract class Binary extends Instruction {
  Object arg0, arg1;

  Binary(Object arg0, Object arg1) {
    this.arg0 = arg0;
    this.arg1 = arg1;
  }

  @Override
  String type() {
    return Etc.typeof(arg0);
  }

  @Override
  void set(int i, Object a) {
    switch (i) {
      case 0 -> arg0 = a;
      case 1 -> arg1 = a;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    }
  }

  @Override
  Object get(int i) {
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

  private static Object evals(Binary op, List<Object> s, List<Object> t) {
    var r = new Object[s.size()];
    for (var i = 0; i < r.length; i++) r[i] = eval(op, s.get(i), t.get(i));
    return Arrays.asList(r);
  }

  @SuppressWarnings("ConstantConditions")
  static Object eval(Binary op, Object a, Object b) {
    // atoms
    do {
      if (a instanceof BigInteger a1) {
        switch (b) {
          case BigInteger b1 -> {
            return op.apply(a1, b1);
          }
          case Float b1 -> {
            return op.apply(a1.floatValue(), b1);
          }
          case Double b1 -> {
            return op.apply(a1.doubleValue(), b1);
          }
          case BigRational b1 -> {
            return op.apply(BigRational.of(a1), b1);
          }
          case Boolean b1 -> {
            return op.apply(a1, b1 ? BigInteger.ONE : BigInteger.ZERO);
          }
          default -> {}
        }
        break;
      }
      if (a instanceof Float a1) {
        if (b instanceof Float b1) return op.apply(a1, b1);
        if (b instanceof Double b1) return op.apply(a1, b1);
        if (b instanceof BigInteger b1) return op.apply(a1, b1.floatValue());
        if (b instanceof BigRational b1) return op.apply(a1, b1.floatValue());
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? 1.0f : 0.0f);
        break;
      }
      if (a instanceof Double a1) {
        if (b instanceof Double b1) return op.apply(a1, b1);
        if (b instanceof Float b1) return op.apply(a1, b1);
        if (b instanceof BigInteger b1) return op.apply(a1, b1.doubleValue());
        if (b instanceof BigRational b1) return op.apply(a1, b1.doubleValue());
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? 1.0 : 0.0);
        break;
      }
      if (a instanceof BigRational a1) {
        if (b instanceof BigRational b1) return op.apply(a1, b1);
        if (b instanceof BigInteger b1) return op.apply(a1, BigRational.of(b1));
        if (b instanceof Float b1) return op.apply(a1.floatValue(), b1);
        if (b instanceof Double b1) return op.apply(a1.doubleValue(), b1);
        if (b instanceof Boolean b1) return op.apply(a1, b1 ? BigRational.ONE : BigRational.ZERO);
        break;
      }
      if (a instanceof Boolean a1) {
        if (b instanceof BigInteger b1) return op.apply(a1 ? BigInteger.ONE : BigInteger.ZERO, b1);
        if (b instanceof Boolean b1)
          return op.apply(
              a1 ? BigInteger.ONE : BigInteger.ZERO, b1 ? BigInteger.ONE : BigInteger.ZERO);
        if (b instanceof Float b1) return op.apply(a1 ? 1.0f : 0.0f, b1);
        if (b instanceof Double b1) return op.apply(a1 ? 1.0 : 0.0, b1);
        if (b instanceof BigRational b1)
          return op.apply(a1 ? BigRational.ONE : BigRational.ZERO, b1);
        break;
      }
    } while (false);

    // lists
    // TODO refactor?
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
  public final Iterator<Object> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        assert i >= 0;
        return i < 2;
      }

      @Override
      public Object next() {
        return get(i++);
      }
    };
  }
}
