package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Goto extends Term {
  final Block target;

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void dbg(Map<Object, Integer> refs) {
    System.out.print("goto " + target);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    mv.visitJumpInsn(GOTO, target.label);
  }

  Goto(Loc loc, Block target) {
    super(loc);
    this.target = target;
  }

  @Override
  Tag tag() {
    return Tag.GOTO;
  }
}
