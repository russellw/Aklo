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

  @Override
  public Term eval() {
    if (arg0 instanceof ConstDouble || arg1 instanceof ConstDouble)
      return new ConstDouble(loc, apply(arg0.doubleVal(), arg1.doubleVal()));
    if (arg0 instanceof ConstFloat || arg1 instanceof ConstFloat)
      return new ConstFloat(loc, apply(arg0.floatVal(), arg1.floatVal()));
    if (arg0 instanceof ConstRational || arg1 instanceof ConstRational)
      return new ConstRational(loc, apply(arg0.rationalVal(), arg1.rationalVal()));
    return new ConstInteger(loc, apply(arg0.integerVal(), arg1.integerVal()));
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
