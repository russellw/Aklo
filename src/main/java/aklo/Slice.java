package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.List;
import org.objectweb.asm.MethodVisitor;

@SuppressWarnings("unchecked")
public final class Slice extends Term3 {
  public Slice(Loc loc, Term s, Term i, Term j) {
    super(loc, s, i, j);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    arg2.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Slice",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;",
        false);
  }

  public static List<Object> eval(Object s, Object i, Object j) {
    return ((List) s).subList(Etc.intVal(i), Etc.intVal(j));
  }

  @Override
  public Tag tag() {
    return Tag.SLICE;
  }
}
