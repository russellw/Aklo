package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public final class InstanceOf extends Term1 {
  public final Type type;

  @Override
  public void emit(MethodVisitor mv) {
    arg.load(mv);
    var s = type.toString();
    mv.visitTypeInsn(INSTANCEOF, s.substring(1, s.length() - 1));
    mv.visitMethodInsn(
        INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
  }

  public InstanceOf(Loc loc, Term arg, Type type) {
    super(loc, arg);
    this.type = type;
  }

  @Override
  public Tag tag() {
    return Tag.INSTANCE_OF;
  }
}
