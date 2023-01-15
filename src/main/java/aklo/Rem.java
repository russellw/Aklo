package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Rem extends Binary {
  Rem(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  Object apply(BigInteger a, BigInteger b) {
    return a.remainder(b);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    Term.load(refs, mv, arg0);
    Term.load(refs, mv, arg1);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "rem",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Tag tag() {
    return Tag.REM;
  }
}
