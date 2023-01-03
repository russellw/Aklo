package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import org.objectweb.asm.MethodVisitor;

public final class True extends Term {
  public True(Loc loc) {
    super(loc);
  }

  @Override
  public double doubleVal() {
    return 1.0;
  }

  @Override
  public float floatVal() {
    return 1.0f;
  }

  @Override
  public BigInteger integerVal() {
    return BigInteger.ONE;
  }

  @Override
  public BigRational rationalVal() {
    return BigRational.ONE;
  }

  @Override
  public Tag tag() {
    return Tag.TRUE;
  }

  @Override
  public Type type() {
    return Type.BOOL;
  }

  @Override
  public void load(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
  }
}
