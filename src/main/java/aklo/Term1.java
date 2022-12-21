package aklo;

import java.util.Iterator;

public abstract class Term1 extends Term {
  public final Term arg;

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
  public Term get(int i) {
    assert i == 0;
    return arg;
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
