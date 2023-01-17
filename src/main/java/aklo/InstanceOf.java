package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class InstanceOf extends Unary {
  final String type;

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg);
    mv.visitTypeInsn(INSTANCEOF, type);
    mv.visitMethodInsn(
        INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
  }

  InstanceOf(Object arg, String type) {
    super(arg);
    this.type = type;
  }
}
