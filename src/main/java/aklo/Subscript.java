package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Subscript extends Binary {
  Subscript(Loc loc, Object arg0, Object arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg0);
    load(refs, mv, arg1);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "subscript",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }
}
