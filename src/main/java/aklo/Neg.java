package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

final class Neg extends Term1 {
  Neg(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Etc", "neg", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  @Override
  double apply(double a) {
    return -a;
  }

  @Override
  float apply(float a) {
    return -a;
  }

  @Override
  BigInteger apply(BigInteger a) {
    return a.negate();
  }

  @Override
  BigRational apply(BigRational a) {
    return a.negate();
  }

  @Override
  Tag tag() {
    return Tag.NEG;
  }
}
