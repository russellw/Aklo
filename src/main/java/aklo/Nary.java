package aklo;

import java.util.Iterator;
import java.util.List;

abstract class Nary extends Term {
  private final Term[] terms;

  Nary(Loc loc, Term[] terms) {
    super(loc);
    this.terms = terms;
  }

  @Override
  void set(int i, Term a) {
    terms[i] = a;
  }

  Nary(Loc loc, List<Term> terms) {
    super(loc);
    this.terms = terms.toArray(new Term[0]);
  }

  @Override
  public int size() {
    return terms.length;
  }

  @Override
  Term get(int i) {
    return terms[i];
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
