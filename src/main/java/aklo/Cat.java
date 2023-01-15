package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

final class Cat extends Term2 {
  Cat(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Etc",
        "cat",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;",
        false);
  }

  @Override
  Tag tag() {
    return Tag.CAT;
  }
}
