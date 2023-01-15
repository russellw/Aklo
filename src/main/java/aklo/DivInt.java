package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class DivInt extends Term2 {
  DivInt(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "divInt",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.divide(b);
  }

  @Override
  Tag tag() {
    return Tag.DIV_INT;
  }
}
