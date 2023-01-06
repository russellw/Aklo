package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class Div extends Term2 {
  public Div(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Div",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  public static Object eval(Object a, Object b) {
    return Term2.eval(new Div(null, null, null), a, b);
  }

  @Override
  public Object apply(double a, double b) {
    return a / b;
  }

  @Override
  public Object apply(float a, float b) {
    return a / b;
  }

  @Override
  public Object apply(BigInteger a, BigInteger b) {
    return BigRational.of(a, b);
  }

  @Override
  public Object apply(BigRational a, BigRational b) {
    return a.divide(b);
  }

  @Override
  public Tag tag() {
    return Tag.DIV;
  }
}
