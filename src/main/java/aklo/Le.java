package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class Le extends Term2 {
  Le(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  Tag tag() {
    return Tag.LE;
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "le",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Object apply(double a, double b) {
    return a <= b;
  }

  @Override
  Object apply(float a, float b) {
    return a <= b;
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.compareTo(b) <= 0;
  }

  @Override
  Object apply(BigRational a, BigRational b) {
    return a.compareTo(b) <= 0;
  }
}
