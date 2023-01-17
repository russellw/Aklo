package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Assign extends Binary {
  Assign(Object arg0, Object arg1) {
    super(arg0, arg1);
  }

  @Override
  String type() {
    return "V";
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    load(refs, mv, arg1);
    var i = refs.get(arg0);
    if (i == null) {
      var y = (Var) arg0;
      mv.visitFieldInsn(PUTSTATIC, "a", y.name, y.type);
      return;
    }
    mv.visitVarInsn(ASTORE, i);
  }
}
