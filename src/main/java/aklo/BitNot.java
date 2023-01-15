package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class BitNot extends Unary {
  BitNot(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Etc", "bitNot", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
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
