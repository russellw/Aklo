package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public final class Throw extends Term1 {
  @Override
  public boolean isTerminator() {
    return true;
  }

  public Throw(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public void emit(MethodVisitor mv) {
    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
    mv.visitInsn(DUP);
    arg.load(mv);
    // TODO: convert to String
    mv.visitMethodInsn(
        INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
    mv.visitInsn(ATHROW);
  }

  @Override
  public Tag tag() {
    return Tag.THROW;
  }
}
