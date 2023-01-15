package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Cmp extends Binary {
  Cmp(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  Tag tag() {
    return Tag.CMP;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    Term.load(refs, mv, arg0);
    Term.load(refs, mv, arg1);
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
