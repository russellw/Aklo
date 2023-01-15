package aklo;

import static org.objectweb.asm.Opcodes.*;

final class Const extends Term {
  final Object val;

  Const(Loc loc, Object val) {
    // TODO does this need location?
    // or is this class even necessary?
    super(loc);
    assert !(val instanceof Integer);
    this.val = val;
  }

  @Override
  public String toString() {
    return val.toString();
  }

  @Override
  Tag tag() {
    return Tag.CONST;
  }

  @Override
  String type() {
    return Etc.typeof(val);
  }
}
