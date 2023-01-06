package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class BitAnd extends Term2 {
  public BitAnd(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/BitAnd",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  public static Object eval(Object a, Object b) {
    return Term2.eval(new BitAnd(null, null, null), a, b);
  }

  @Override
  public Object apply(BigInteger a, BigInteger b) {
    return a.and(b);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_AND;
  }
}
