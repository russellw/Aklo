package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class ConstInteger extends Term {
  public final BigInteger val;

  public ConstInteger(Loc loc, BigInteger val) {
    super(loc);
    this.val = val;
  }

  @Override
  public void load(MethodVisitor mv) {
    try {
      mv.visitLdcInsn(val.longValueExact());
      mv.visitMethodInsn(
          INVOKESTATIC, "java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;", false);
    } catch (ArithmeticException e) {
      // okay to use an exception for something that is not an error here
      // because constant integers outside 2^63 are rare enough
      // that there is no performance impact
      mv.visitTypeInsn(NEW, "java/math/BigInteger");
      mv.visitInsn(DUP);
      mv.visitLdcInsn(val.toString(16));
      mv.visitIntInsn(BIPUSH, 16);
      mv.visitMethodInsn(
          INVOKESPECIAL, "java/math/BigInteger", "<init>", "(Ljava/lang/String;I)V", false);
    }
  }

  @Override
  public Object val() {
    return val;
  }

  @Override
  public String toString() {
    return val.toString();
  }

  public ConstInteger(Loc loc, long val) {
    super(loc);
    this.val = BigInteger.valueOf(val);
  }

  @Override
  public double doubleVal() {
    return val.doubleValue();
  }

  @Override
  public float floatVal() {
    return val.floatValue();
  }

  @Override
  public BigInteger integerVal() {
    return val;
  }

  @Override
  public BigRational rationalVal() {
    return BigRational.of(val);
  }

  @Override
  public int intValExact() {
    return val.intValueExact();
  }

  @Override
  public Tag tag() {
    return Tag.INTEGER;
  }

  @Override
  public Type type() {
    return Type.INTEGER;
  }
}
