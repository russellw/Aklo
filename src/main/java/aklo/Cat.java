package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

import java.util.Map;

final class Cat extends Binary {
  Cat(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    arg0.load(, mv);
    arg1.load(, mv);
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
