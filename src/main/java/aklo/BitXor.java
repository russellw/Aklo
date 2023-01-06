package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class BitXor extends Term2 {
  public BitXor(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/BitXor",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  public static Object eval(Object a, Object b) {
    return Term2.eval(new BitXor(null, null, null), a, b);
  }

  @Override
  public Object apply(BigInteger a, BigInteger b) {
    return a.xor(b);
  }

  @Override
  public Tag tag() {
    return Tag.BIT_XOR;
  }
}
