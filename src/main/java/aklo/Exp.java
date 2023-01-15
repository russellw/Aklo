package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class Exp extends Term2 {
  Exp(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "exp",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Object apply(double a, double b) {
    return Math.pow(a, b);
  }

  @Override
  Object apply(float a, float b) {
    return Math.pow(a, b);
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.pow(b.intValueExact());
  }

  @Override
  Object apply(BigRational a, BigRational b) {
    return Math.pow(a.doubleValue(), b.doubleValue());
  }

  @Override
  Tag tag() {
    return Tag.EXP;
  }
}
