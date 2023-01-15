package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class Mul extends Term2 {
  Mul(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Mul",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  static Object eval(Object a, Object b) {
    return Term2.eval(new Mul(null, null, null), a, b);
  }

  @Override
  Object apply(double a, double b) {
    return a * b;
  }

  @Override
  Object apply(float a, float b) {
    return a * b;
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.multiply(b);
  }

  @Override
  Object apply(BigRational a, BigRational b) {
    return a.multiply(b);
  }

  @Override
  Tag tag() {
    return Tag.MUL;
  }
}
