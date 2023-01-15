package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;

@SuppressWarnings("unchecked")
final class Slice extends Ternary {
  Slice(Loc loc, Term s, Term i, Term j) {
    super(loc, s, i, j);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    arg0.load(, mv);
    arg1.load(, mv);
    arg2.load(, mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Slice",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;",
        false);
  }

  static List<Object> eval(Object s, Object i, Object j) {
    return ((List) s).subList(Etc.intVal(i), Etc.intVal(j));
  }

  @Override
  Tag tag() {
    return Tag.SLICE;
  }
}
