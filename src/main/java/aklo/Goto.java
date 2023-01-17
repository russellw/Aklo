package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Goto extends Instruction {
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

  Goto(Block target) {
    super();
    this.target = target;
  }
}
