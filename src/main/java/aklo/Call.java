package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.List;
import org.objectweb.asm.MethodVisitor;

public final class Call extends Terms {
  public Call(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  public Call(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.CALL;
  }

  @Override
  public void emit(MethodVisitor mv) {
    for (var i = 1; i < size(); i++) get(i).load(mv);
    var f = (Fn) get(0);
    mv.visitMethodInsn(INVOKESTATIC, "a", f.name, f.descriptor(), false);
  }
}
