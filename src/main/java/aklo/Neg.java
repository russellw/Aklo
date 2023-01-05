package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class Neg extends Term1 {
  public Neg(Loc loc, Term arg) {
    super(loc, arg);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC, "aklo/Neg", "eval", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
  }

  public static Object eval(Object a) {
    return Term1.eval(new Neg(null, null), a);
  }

  @Override
  public double apply(double a) {
    return -a;
  }

  @Override
  public float apply(float a) {
    return -a;
  }

  @Override
  public BigInteger apply(BigInteger a) {
    return a.negate();
  }

  @Override
  public BigRational apply(BigRational a) {
    return a.negate();
  }

  @Override
  public Tag tag() {
    return Tag.NEG;
  }
}
