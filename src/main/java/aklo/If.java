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
  void dbg(Map<Object, Integer> refs) {
    System.out.print("If");
    dbg(refs, get(0));
    System.out.printf(" %s %s", trueTarget, falseTarget);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg);
    mv.visitMethodInsn(INVOKESTATIC, "aklo/Etc", "truth", "(Ljava/lang/Object;)Z", false);
    mv.visitJumpInsn(IFNE, trueTarget.label);
    mv.visitJumpInsn(GOTO, falseTarget.label);
  }

  @Override
  public String toString() {
    return String.format("If %s %s", trueTarget, falseTarget);
  }

  If(Object cond, Block trueTarget, Block falseTarget) {
    super(cond);
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }
}
