package aklo;

import java.util.Iterator;

abstract class Ternary extends Instruction {
  Object arg0, arg1, arg2;

  Ternary(Object arg0, Object arg1, Object arg2) {
    this.arg0 = arg0;
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  @Override
  void set(int i, Object a) {
    switch (i) {
      case 0 -> arg0 = a;
      case 1 -> arg1 = a;
      case 2 -> arg2 = a;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    }
  }

  @Override
  Object get(int i) {
    return switch (i) {
      case 0 -> arg0;
      case 1 -> arg1;
      case 2 -> arg2;
      default -> throw new IndexOutOfBoundsException(Integer.toString(i));
    };
  }

  @Override
  public int size() {
    return 3;
  }

  @Override
  public final Iterator<Object> iterator() {
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        assert i >= 0;
        return i < 3;
      }

      @Override
      public Object next() {
        return get(i++);
      }
    };
  }
}
