package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.objectweb.asm.MethodVisitor;

import java.util.Map;

final class Len extends Unary {
  Len(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    arg.load(, mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Etc", "len", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  @Override
  Tag tag() {
    return Tag.LEN;
  }
}
