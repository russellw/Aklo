package aklo;

import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.MethodVisitor;

public final class ReturnVoid extends Term {
  public ReturnVoid(Loc loc) {
    super(loc);
  }

  @Override
  public boolean isTerminator() {
    return true;
  }

  @Override
  public void emit(MethodVisitor mv) {
    mv.visitInsn(RETURN);
  }

  @Override
  public Tag tag() {
    return Tag.RETURN;
  }
}
