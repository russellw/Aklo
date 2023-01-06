package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import java.util.List;
import org.objectweb.asm.MethodVisitor;

public final class Len extends Term1 {
  public Len(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Len", "eval", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  public static Object eval(Object s) {
    return BigInteger.valueOf(((List) s).size());
  }

  @Override
  public Tag tag() {
    return Tag.LEN;
  }
}
