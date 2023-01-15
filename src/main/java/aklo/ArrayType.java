package aklo;

final class ArrayType extends Type {
  final Type elem;

  ArrayType(Type elem) {
    this.elem = elem;
  }

  @Override
  String descriptor() {
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
  Kind kind() {
    return Kind.ARRAY;
  }
}
