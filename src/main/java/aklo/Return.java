package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

final class Return extends Term1 {
  Return(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitInsn(ARETURN);
  }

  @Override
  Tag tag() {
    return Tag.RETURN;
  }
}
