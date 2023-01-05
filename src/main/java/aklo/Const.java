package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class Const extends Term {
  public final Object val;

  public Const(Loc loc, Object val) {
    super(loc);
    assert !(val instanceof Integer);
    this.val = val;
  }

  @Override
  public void load(MethodVisitor mv) {
    if (val instanceof BigInteger x) {
      try {
        mv.visitLdcInsn(x.longValueExact());
        mv.visitMethodInsn(
            INVOKESTATIC, "java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;", false);
      } catch (ArithmeticException e) {
        // okay to use an exception for something that is not an error here
        // because constant integers outside 2^63 are rare enough
        // that there is no performance impact
        mv.visitTypeInsn(NEW, "java/math/BigInteger");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(x.toString(16));
        mv.visitIntInsn(BIPUSH, 16);
        mv.visitMethodInsn(
            INVOKESPECIAL, "java/math/BigInteger", "<init>", "(Ljava/lang/String;I)V", false);
      }
      return;
    }
    if (val instanceof Boolean x) {
      mv.visitFieldInsn(
          GETSTATIC, "java/lang/Boolean", x ? "TRUE" : "FALSE", "Ljava/lang/Boolean;");
      return;
    }
    if (val instanceof BigRational x) {
      throw new UnsupportedOperationException(toString());
    }
    mv.visitLdcInsn(val);
    if (val instanceof Float) {
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
      return;
    }
    if (val instanceof Double) {
      mv.visitMethodInsn(
          INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    }
  }

  @Override
  public String toString() {
    return val.toString();
  }

  @Override
  public Tag tag() {
    return Tag.CONST;
  }

  @Override
  public Type type() {
    return Type.INTEGER;
  }
}
