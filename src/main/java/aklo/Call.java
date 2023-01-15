package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.List;
import org.objectweb.asm.MethodVisitor;

final class Call extends Terms {
  Call(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  Call(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  @Override
  Tag tag() {
    return Tag.CALL;
  }

  @Override
  Type type() {
    var f = (Fn) get(0);
    return f.rtype;
  }

  @Override
  void emit(MethodVisitor mv) {
    for (var i = 1; i < size(); i++) get(i).load(mv);
    var f = (Fn) get(0);
    mv.visitMethodInsn(INVOKESTATIC, "a", f.name, f.descriptor(), false);
  }
}
