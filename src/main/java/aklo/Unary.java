package aklo;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
abstract class Unary extends Instruction {
  Object arg;

  Unary(Object arg) {
    this.arg = arg;
  }

  @Override
  Type type() {
    return Etc.typeof(arg);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  void set(int i, Object a) {
    assert i == 0;
    arg = a;
  }

  @Override
  Object get(int i) {
    assert i == 0;
    return arg;
  }

  private static List<Object> evals(Unary op, List<Object> s) {
    var r = new Object[s.size()];
    for (var i = 0; i < r.length; i++) r[i] = eval(op, s.get(i));
    return Arrays.asList(r);
  }

  static Object eval(Unary op, Object a0) {
    return switch (a0) {
      case BigInteger a -> op.apply(a);
      case Float a -> op.apply(a);
      case Double a -> op.apply(a);
      case BigRational a -> op.apply(a);
      case Boolean a -> op.apply(a ? BigInteger.ONE : BigInteger.ZERO);
      case List a -> evals(op, a);
      default -> throw new IllegalArgumentException(String.format("%s(%s)", op, a0));
    };
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
  public final Iterator<Object> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        return i == 0;
      }

      @Override
      public Object next() {
        assert i == 0;
        i++;
        return arg;
      }
    };
  }
}
