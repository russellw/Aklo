package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public final class False extends Term {
  public False(Loc loc) {
    super(loc);
  }

  @Override
  public void load(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
  }

  @Override
  public Tag tag() {
    return Tag.FALSE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }
}
