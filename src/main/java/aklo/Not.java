package aklo;

final class Not extends Term1 {
  Not(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  Tag tag() {
    return Tag.NOT;
  }

  @Override
  Type type() {
    return Type.BOOL;
  }
}
