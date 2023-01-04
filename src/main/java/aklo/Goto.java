package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

public final class Goto extends Term {
  public final Block target;

  @Override
  public boolean isTerminator() {
    return true;
  }

  @Override
  public void dbg(Map<Term, Integer> refs) {
    super.dbg(refs);
    System.out.print(" " + target);
  }

  @Override
  public void emit(MethodVisitor mv) {
    mv.visitJumpInsn(GOTO, target.label);
  }

  public Goto(Loc loc, Block target) {
    super(loc);
    this.target = target;
  }

  @Override
  public Tag tag() {
    return Tag.GOTO;
  }
}
