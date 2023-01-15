package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Return extends Unary {
  Return(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    arg.load(refs, mv);
    mv.visitInsn(ARETURN);
  }

  @Override
  Tag tag() {
    return Tag.RETURN;
  }
}
