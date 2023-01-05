package aklo;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class Add extends Term2 {
  public Add(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg0.load(mv);
    arg1.load(mv);
    mv.visitMethodInsn(
        INVOKESTATIC,
        "aklo/Add",
        "eval",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        false);
  }

  public static Object eval(Object a, Object b) {
    return Term2.eval(new Add(null, null, null), a, b);
  }

  @Override
  public double apply(double a, double b) {
    return a + b;
  }

  @Override
  public float apply(float a, float b) {
    return a + b;
  }

  @Override
  public Object apply(BigInteger a, BigInteger b) {
    return a.add(b);
  }

  @Override
  public BigRational apply(BigRational a, BigRational b) {
    return a.add(b);
  }

  @Override
  public Tag tag() {
    return Tag.ADD;
  }
}
