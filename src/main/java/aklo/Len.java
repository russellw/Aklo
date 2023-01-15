package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Len extends Unary {
  Len(Loc loc, Object arg) {
    super(loc, arg);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Etc", "len", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  @Override
  Tag tag() {
    return Tag.LEN;
  }
}
