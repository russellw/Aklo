package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Subscript extends Binary {
  Subscript(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    arg0.load(refs, mv);
    arg1.load(refs, mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "subscript",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Tag tag() {
    return Tag.SUBSCRIPT;
  }
}
