package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public final class Return extends Term1 {
  public Return(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public boolean isTerminator() {
    return true;
  }

  @Override
  public void emit(MethodVisitor mv) {
    // TODO
    mv.visitInsn(RETURN);
  }

  @Override
  public Tag tag() {
    return Tag.RETURN;
  }
}
