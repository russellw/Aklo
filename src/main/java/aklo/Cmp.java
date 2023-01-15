package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class Cmp extends Term2 {
  Cmp(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  Tag tag() {
    return Tag.CMP;
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "cmp",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Object apply(double a, double b) {
    return BigInteger.valueOf(Double.compare(a, b));
  }

  @Override
  Object apply(float a, float b) {
    return BigInteger.valueOf(Float.compare(a, b));
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return BigInteger.valueOf(a.compareTo(b));
  }

  @Override
  Object apply(BigRational a, BigRational b) {
    return BigInteger.valueOf(a.compareTo(b));
  }
}
