package aklo;

import java.util.Iterator;

public abstract class Terms extends Term {
  private final Term[] terms;

  public Terms(Loc loc, Term[] terms) {
    super(loc);
    this.terms = terms;
  }

  @Override
  public int size() {
    return terms.length;
  }

  @Override
  public final Iterator<Term> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        return i < terms.length;
      }

      @Override
      public Term next() {
        return terms[i++];
      }
    };
  }
}
