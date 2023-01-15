package aklo;

import static org.objectweb.asm.Opcodes.ASTORE;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Assign extends Binary {
  Assign(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  Type type() {
    return Type.VOID;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg1);
    var i = refs.get(arg0);
    if (i == null) throw new IllegalStateException(String.format("%s: %s", loc, this));
    mv.visitVarInsn(ASTORE, i);
  }

  @Override
  Tag tag() {
    return Tag.ASSIGN;
  }
}
