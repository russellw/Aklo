package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Throw extends Unary {
  @Override
  boolean isTerminator() {
    return true;
  }

  Throw(Object arg) {
    super(arg);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
    mv.visitInsn(DUP);
    load(refs, mv, arg);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Etc", "decode", "(Ljava/lang/Object;)Ljava/lang/String;", false);
    mv.visitMethodInsn(
        INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
    mv.visitInsn(ATHROW);
  }
}
