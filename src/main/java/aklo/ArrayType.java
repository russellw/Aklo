package aklo;

public final class ArrayType extends Type {
  public final Type elem;

  public ArrayType(Type elem) {
    this.elem = elem;
  }

  @Override
  public String descriptor() {
    return '[' + elem.descriptor();
  }

  @Override
  public Type get(int i) {
    assert i == 0;
    return elem;
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Kind kind() {
    return Kind.ARRAY;
  }
}
