package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class If extends Term1 {
  final Block trueTarget, falseTarget;

  @Override
  boolean isTerminator() {
    return true;
  }

  @Override
  void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(INVOKESTATIC, "aklo/Etc", "truth", "(Ljava/lang/Object;)Z", false);
    mv.visitJumpInsn(IFNE, trueTarget.label);
    mv.visitJumpInsn(GOTO, falseTarget.label);
  }

  @Override
  void dbg(Map<Object, Integer> refs) {
    super.dbg(refs);
    System.out.printf(" %s %s", trueTarget, falseTarget);
  }

  If(Loc loc, Term cond, Block trueTarget, Block falseTarget) {
    super(loc, cond);
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }

  @Override
  Tag tag() {
    return Tag.IF;
  }
}
