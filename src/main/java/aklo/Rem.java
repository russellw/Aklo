package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Rem extends Binary {
  Rem(Object arg0, Object arg1) {
    super(arg0, arg1);
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.remainder(b);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg0);
    load(refs, mv, arg1);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "rem",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }
}
