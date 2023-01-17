package aklo;

final class GetField extends Unary {
  final String owner;
  final String name;
  final String descriptor;

  GetField(String owner, String name, String descriptor, Object arg) {
    super(arg);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }
}
