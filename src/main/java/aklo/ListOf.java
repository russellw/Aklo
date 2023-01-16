package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class ListOf extends Nary {
  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    var n = size();
    if (n <= 10) {
      for (var a : this) load(refs, mv, a);
      mv.visitMethodInsn(
          INVOKESTATIC,
          "java/util/List",
          "of",
          '(' + "Ljava/lang/Object;".repeat(n) + ")Ljava/util/List;",
          true);
      return;
    }
    emitInt(mv, n);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    for (var i = 0; i < n; i++) {
      mv.visitInsn(DUP);
      emitInt(mv, i);
      load(refs, mv, get(i));
      mv.visitInsn(AASTORE);
    }
    mv.visitMethodInsn(
        INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false);
  }

  ListOf(Loc loc, Object... args) {
    super(loc, args);
  }

  @Override
  String type() {
    return "Ljava/util/List;";
  }
}
