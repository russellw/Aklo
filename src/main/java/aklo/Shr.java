package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class Shr extends Binary {
  Shr(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Shr",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  static Object eval(Object a, Object b) {
    return Binary.eval(new Shr(null, null, null), a, b);
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.shiftRight(b.intValueExact());
  }

  @Override
  Tag tag() {
    return Tag.SHR;
  }
}
