package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Sub extends Binary {
  Sub(Object arg0, Object arg1) {
    super(arg0, arg1);
  }

  @Override
  Object apply(double a, double b) {
    return a - b;
  }

  @Override
  Object apply(float a, float b) {
    return a - b;
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.subtract(b);
  }

  @Override
  Object apply(BigRational a, BigRational b) {
    return a.subtract(b);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg0);
    load(refs, mv, arg1);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "sub",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }
}
