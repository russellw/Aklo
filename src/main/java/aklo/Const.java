package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;

final class Const extends Term {
  static final Const ZERO = new Const(null, BigInteger.ZERO);
  static final Const ONE = new Const(null, BigInteger.ONE);
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
