package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class BitNot extends Term1 {
  BitNot(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/BitNot", "eval", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  static Object eval(Object a) {
    return Term1.eval(new BitNot(null, null), a);
  }

  @Override
  BigInteger apply(BigInteger a) {
    return a.not();
  }

  @Override
  Tag tag() {
    return Tag.BIT_NOT;
  }
}
