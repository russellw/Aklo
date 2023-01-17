package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Not extends Unary {
  Not(Object arg) {
    super(arg);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Etc", "bitNot", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  @Override
  BigInteger apply(BigInteger a) {
    return a.not();
  }
}
