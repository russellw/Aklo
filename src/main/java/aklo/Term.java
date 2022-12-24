package aklo;

import java.math.BigInteger;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Term extends AbstractCollection<Term> {
  public final Loc loc;

  public Term(Loc loc) {
    this.loc = loc;
  }

  public abstract Tag tag();

  public final void walk(Consumer<Term> f) {
    f.accept(this);
    for (var a : this) a.walk(f);
  }

  public void set(int i, Term a) {
    throw new UnsupportedOperationException(toString());
  }

  public Type type() {
    throw new UnsupportedOperationException(toString());
  }

  public Term eval() {
    throw new UnsupportedOperationException(toString());
  }

  public Term get(int i) {
    throw new UnsupportedOperationException(toString());
  }

  public double doubleVal() {
    throw new UnsupportedOperationException(toString());
  }

  public float floatVal() {
    throw new UnsupportedOperationException(toString());
  }

  public BigInteger integerVal() {
    throw new UnsupportedOperationException(toString());
  }

  public BigRational rationalVal() {
    throw new UnsupportedOperationException(toString());
  }

  public int intValExact() {
    throw new UnsupportedOperationException(toString());
  }

  @Override
  public int size() {
    return 0;
  }

  public Term map(Function<Term, Term> f) {
    assert isEmpty();
    return this;
  }

  @Override
  public Iterator<Term> iterator() {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public Term next() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
