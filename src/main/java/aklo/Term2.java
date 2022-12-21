package aklo;

import java.util.Iterator;

public abstract class Term2 extends Term {
  public final Term arg0, arg1;

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
  public Term get(int i) {
    assert 0 <= i && i < 2;
    if (i == 0) return arg0;
    return arg1;
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
