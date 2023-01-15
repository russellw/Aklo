package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

import java.util.Map;

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
    arg.load(, mv);
    mv.visitInsn(ARETURN);
  }

  @Override
  Tag tag() {
    return Tag.RETURN;
  }
}
