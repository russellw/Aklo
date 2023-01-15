package aklo;

import java.util.Iterator;

abstract class Term3 extends Term {
  Term arg0, arg1, arg2;

  Term3(Loc loc, Term arg0, Term arg1, Term arg2) {
    super(loc);
    this.arg0 = arg0;
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  @Override
  void set(int i, Term a) {
    switch (i) {
      case 0 -> arg0 = a;
      case 1 -> arg1 = a;
      case 2 -> arg2 = a;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    }
  }

  @Override
  Term get(int i) {
    return switch (i) {
      case 0 -> arg0;
      case 1 -> arg1;
      case 2 -> arg2;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    };
  }

  @Override
  Type type() {
    return Type.ANY;
  }

  @Override
  public int size() {
    return 3;
  }

  @Override
  public final Iterator<Term> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        assert i >= 0;
        return i < 3;
      }

      @Override
      public Term next() {
        return get(i++);
      }
    };
  }
}
