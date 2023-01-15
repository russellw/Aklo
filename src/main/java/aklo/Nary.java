package aklo;

import java.util.Iterator;
import java.util.List;

abstract class Nary extends Insn {
  private final Object[] args;

  Nary(Loc loc, Object[] args) {
    super(loc);
    this.args = args;
  }

  @Override
  void set(int i, Object a) {
    args[i] = a;
  }

  Nary(Loc loc, List<Object> args) {
    super(loc);
    this.args = args.toArray();
  }

  @Override
  public int size() {
    return args.length;
  }

  @Override
  Object get(int i) {
    return args[i];
  }

  @Override
  public final Iterator<Object> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        return i < args.length;
      }

      @Override
      public Object next() {
        return args[i++];
      }
    };
  }
}
