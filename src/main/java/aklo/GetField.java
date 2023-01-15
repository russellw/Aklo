package aklo;

final class GetField extends Unary {
  final String owner;
  final String name;
  final String descriptor;

  GetField(Loc loc, String owner, String name, String descriptor, Term arg) {
    super(loc, arg);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  Tag tag() {
    return Tag.GET_FIELD;
  }
}
