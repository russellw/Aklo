package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

public final class If extends Term1 {
  public final Block trueTarget, falseTarget;

  @Override
  public boolean isTerminator() {
    return true;
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(INVOKESTATIC, "a", "truth", "(Ljava/lang/Object;)Z", false);
    mv.visitJumpInsn(IFNE, trueTarget.label);
    mv.visitJumpInsn(GOTO, falseTarget.label);
  }

  @Override
  public void dbg(Map<Term, Integer> refs) {
    super.dbg(refs);
    System.out.printf(", %s, %s", trueTarget, falseTarget);
  }

  public If(Loc loc, Term cond, Block trueTarget, Block falseTarget) {
    super(loc, cond);
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }

  @Override
  public Tag tag() {
    return Tag.IF;
  }
}
