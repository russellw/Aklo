package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Add extends Binary {
  Add(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    arg0.load(refs, mv);
    arg1.load(refs, mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "add",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Object apply(double a, double b) {
    return a + b;
  }

  @Override
  Object apply(float a, float b) {
    return a + b;
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.add(b);
  }

  @Override
  Object apply(BigRational a, BigRational b) {
    return a.add(b);
  }

  @Override
  Tag tag() {
    return Tag.ADD;
  }
}
