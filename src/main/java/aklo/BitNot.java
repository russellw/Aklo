package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class BitNot extends Term1 {
  public BitNot(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/BitNot", "eval", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  public static Object eval(Object a) {
    return Term1.eval(new BitNot(null, null), a);
  }

  @Override
  public BigInteger apply(BigInteger a) {
    return a.not();
  }

  @Override
  public Tag tag() {
    return Tag.BIT_NOT;
  }
}
