package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.List;
import org.objectweb.asm.MethodVisitor;

public final class Subscript extends Term2 {
  public Subscript(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Subscript",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  public static Object eval(Object s, Object i) {
    return ((List) s).get(Etc.intVal(i));
  }

  @Override
  public Tag tag() {
    return Tag.SUBSCRIPT;
  }
}
