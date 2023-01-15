package aklo;

import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.MethodVisitor;

import java.util.Map;

final class ReturnVoid extends Term {
  ReturnVoid(Loc loc) {
    super(loc);
  }

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    mv.visitInsn(RETURN);
  }

  @Override
  Tag tag() {
    return Tag.RETURN;
  }
}
