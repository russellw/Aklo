package aklo;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.function.Function;

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
    assert 0 <= i && i < 2;
    if (i == 0) arg0 = a;
    else arg1 = a;
  }

  @Override
  public Term get(int i) {
    assert 0 <= i && i < 2;
    if (i == 0) return arg0;
    return arg1;
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
  public Term map(Function<Term, Term> f) {
    var a = f.apply(arg0);
    var b = f.apply(arg1);
    return remake(loc, a, b);
  }

  public abstract Term remake(Loc loc, Term arg0, Term arg1);

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
        return i < 2;
      }

      @Override
      public Term next() {
        assert 0 <= i && i < 2;
        if (i++ == 0) return arg0;
        return arg1;
      }
    };
  }
}
