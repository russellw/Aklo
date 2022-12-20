package aklo;

import java.util.AbstractCollection;
import java.util.Iterator;

public abstract class Term extends AbstractCollection<Term> {
  public final Loc loc;

  public Term(Loc loc) {
    this.loc = loc;
  }

  public abstract Tag tag();

  public abstract Type type();

  @Override
  public int size() {
    return 0;
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
        throw new IllegalArgumentException();
      }
    };
  }
}
