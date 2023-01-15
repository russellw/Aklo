package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.objectweb.asm.MethodVisitor;

final class Subscript extends Term2 {
  Subscript(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "subscript",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  @Override
  Tag tag() {
    return Tag.SUBSCRIPT;
  }
}
