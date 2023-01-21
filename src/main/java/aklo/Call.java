package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Call extends Nary {
  Call(Object... args) {
    super(args);
  }

  @Override
  String type() {
    var f = get(0);
    if (f instanceof Fn f1) return f1.rtype;
    return "Ljava/lang/Object;";
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    var f = get(0);
    if (f instanceof Fn f1) {
      for (var i = 1; i < size(); i++) load(refs, mv, get(i));
      mv.visitMethodInsn(INVOKESTATIC, "a", f1.name, f1.descriptor(), false);
      return;
    }
    for (var i = 0; i < size(); i++) load(refs, mv, get(i));
    switch (size() - 1) {
      case 1 -> mv.visitMethodInsn(
          INVOKEINTERFACE,
          "java/util/function/UnaryOperator",
          "apply",
          "(Ljava/lang/Object;)Ljava/lang/Object;",
          true);
      case 2 -> mv.visitMethodInsn(
          INVOKEINTERFACE,
          "java/util/function/BinaryOperator",
          "apply",
          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
          true);
      default -> throw new IllegalArgumentException(this + Arrays.toString(args));
    }
  }
}
