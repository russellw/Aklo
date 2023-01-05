package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public final class True extends Term {
  public True(Loc loc) {
    super(loc);
  }

  @Override
  public Tag tag() {
    return Tag.TRUE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }

  @Override
  public void load(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
  }
}
