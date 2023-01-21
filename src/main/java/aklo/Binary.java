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
  static Object eval(Binary op, Object a0, Object b0) {
    // atoms
    do {
      if (a0 instanceof BigInteger a) {
        switch (b0) {
          case BigInteger b -> {
            return op.apply(a, b);
          }
          case Float b -> {
            return op.apply(a.floatValue(), b);
          }
          case Double b -> {
            return op.apply(a.doubleValue(), b);
          }
          case BigRational b -> {
            return op.apply(BigRational.of(a), b);
          }
          case Boolean b -> {
            return op.apply(a, b ? BigInteger.ONE : BigInteger.ZERO);
          }
          case List b -> {
            return evals(op, Collections.nCopies(b.size(), a), b);
          }
          default -> {}
        }
        break;
      }
      if (a0 instanceof Float a) {
        switch (b0) {
          case Float b -> {
            return op.apply(a, b);
          }
          case Double b -> {
            return op.apply(a, b);
          }
          case BigInteger b -> {
            return op.apply(a, b.floatValue());
          }
          case BigRational b -> {
            return op.apply(a, b.floatValue());
          }
          case Boolean b -> {
            return op.apply(a, b ? 1.0f : 0.0f);
          }
          case List b -> {
            return evals(op, Collections.nCopies(b.size(), a), b);
          }
          default -> {}
        }
        break;
      }
      if (a0 instanceof Double a) {
        switch (b0) {
          case Double b -> {
            return op.apply(a, b);
          }
          case Float b -> {
            return op.apply(a, b);
          }
          case BigInteger b -> {
            return op.apply(a, b.doubleValue());
          }
          case BigRational b -> {
            return op.apply(a, b.doubleValue());
          }
          case Boolean b -> {
            return op.apply(a, b ? 1.0 : 0.0);
          }
          case List b -> {
            return evals(op, Collections.nCopies(b.size(), a), b);
          }
          default -> {}
        }
        break;
      }
      if (a0 instanceof BigRational a) {
        switch (b0) {
          case BigRational b -> {
            return op.apply(a, b);
          }
          case BigInteger b -> {
            return op.apply(a, BigRational.of(b));
          }
          case Float b -> {
            return op.apply(a.floatValue(), b);
          }
          case Double b -> {
            return op.apply(a.doubleValue(), b);
          }
          case Boolean b -> {
            return op.apply(a, b ? BigRational.ONE : BigRational.ZERO);
          }
          case List b -> {
            return evals(op, Collections.nCopies(b.size(), a), b);
          }
          default -> {}
        }
        break;
      }
      if (a0 instanceof Boolean a) {
        switch (b0) {
          case BigInteger b -> {
            return op.apply(a ? BigInteger.ONE : BigInteger.ZERO, b);
          }
          case Boolean b -> {
            return op.apply(
                a ? BigInteger.ONE : BigInteger.ZERO, b ? BigInteger.ONE : BigInteger.ZERO);
          }
          case Float b -> {
            return op.apply(a ? 1.0f : 0.0f, b);
          }
          case Double b -> {
            return op.apply(a ? 1.0 : 0.0, b);
          }
          case BigRational b -> {
            return op.apply(a ? BigRational.ONE : BigRational.ZERO, b);
          }
          case List b -> {
            return evals(op, Collections.nCopies(b.size(), a), b);
          }
          default -> {}
        }
        break;
      }
    } while (false);

    // lists
    // TODO refactor?
    do {
      if (a0 instanceof List a) {
        List<Object> b;
        if (b0 instanceof List) {
          b = (List) b0;
          if (a.size() != b.size()) break;
        } else b = Collections.nCopies(a.size(), b0);
        return evals(op, a, b);
      }
      if (b0 instanceof List b) {
        var a = Collections.nCopies(b.size(), a0);
        return evals(op, a, b);
      }
    } while (false);

    throw new IllegalArgumentException(String.format("%s(%s, %s)", op, a0, b0));
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
