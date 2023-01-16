package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.List;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Call extends Nary {
  Call(Loc loc, List<Object> args) {
    super(loc, args);
  }

  @Override
  String type() {
    var f = (Fn) get(0);
    return f.rtype;
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    for (var i = 1; i < size(); i++) load(refs, mv, get(i));
    var f = (Fn) get(0);
    mv.visitMethodInsn(INVOKESTATIC, "a", f.name, f.descriptor(), false);
  }
}
