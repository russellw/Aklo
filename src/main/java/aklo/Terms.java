package aklo;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class Terms extends Term {
  private final Term[] terms;

  public abstract Term remake(Loc loc, Term[] terms);

  public Terms(Loc loc, Term[] terms) {
    super(loc);
    this.terms = terms;
  }

  @Override
  public Term map(Function<Term, Term> f) {
    var r = new Term[terms.length];
    for (var i = 0; i < r.length; i++) r[i] = f.apply(terms[i]);
    return remake(loc, r);
  }

  public Terms(Loc loc, List<Term> terms) {
    super(loc);
    this.terms = terms.toArray(new Term[0]);
  }

  @Override
  public int size() {
    return terms.length;
  }

  @Override
  public Term get(int i) {
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
