package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

final class InstanceOf extends Unary {
  final Type type;

  @Override
  void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitTypeInsn(INSTANCEOF, type.toString());
    mv.visitMethodInsn(
        INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
  }

  InstanceOf(Loc loc, Term arg, Type type) {
    super(loc, arg);
    this.type = type;
  }

  @Override
  Tag tag() {
    return Tag.INSTANCE_OF;
  }
}
