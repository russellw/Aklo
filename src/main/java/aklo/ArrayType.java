package aklo;

final class ArrayType extends Type {
  final Type element;

  private ArrayType(Type element) {
    super(null);
    this.element = element;
  }

  public static ArrayType of(Type element) {
    // TODO intern
    return new ArrayType(element);
  }

  @Override
  public String toString() {
    return "[" + element;
  }
}
