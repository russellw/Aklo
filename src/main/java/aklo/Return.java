package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Return extends Unary {
  Return(Loc loc, Object arg) {
    super(loc, arg);
  }

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg);
    mv.visitInsn(ARETURN);
  }
}
