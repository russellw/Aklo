package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class If extends Unary {
  final Block trueTarget, falseTarget;

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg);
    mv.visitMethodInsn(INVOKESTATIC, "aklo/Etc", "truth", "(Ljava/lang/Object;)Z", false);
    mv.visitJumpInsn(IFNE, trueTarget.label);
    mv.visitJumpInsn(GOTO, falseTarget.label);
  }

  @Override
  void dbg(Map<Object, Integer> refs) {
    super.dbg(refs);
    System.out.printf(" %s %s", trueTarget, falseTarget);
  }

  If(Loc loc, Object cond, Block trueTarget, Block falseTarget) {
    super(loc, cond);
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }
}
