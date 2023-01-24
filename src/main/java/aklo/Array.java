package aklo;

final class Array extends Type {
  final Type element;

  private Array(Type element) {
    super(null);
    this.element = element;
  }

  public static Array of(Type element) {
    // TODO intern
    return new Array(element);
  }

  @Override
  public String toString() {
    return "[" + element;
  }
}
