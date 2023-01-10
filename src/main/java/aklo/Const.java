package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import java.util.List;
import org.objectweb.asm.MethodVisitor;

public final class Const extends Term {
  public static final Const ZERO = new Const(null, BigInteger.ZERO);
  public static final Const ONE = new Const(null, BigInteger.ONE);
  public final Object val;

  public Const(Loc loc, Object val) {
    // TODO does this need location?
    super(loc);
    assert !(val instanceof Integer);
    this.val = val;
  }

  @Override
  public void load(MethodVisitor mv) {
    load(mv, val);
  }

  private static void load(MethodVisitor mv, Object a) {
    // scalars with special logic
    if (a instanceof BigInteger a1) {
      try {
        mv.visitLdcInsn(a1.longValueExact());
        mv.visitMethodInsn(
            INVOKESTATIC, "java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;", false);
      } catch (ArithmeticException e) {
        // okay to use an exception for something that is not an error here
        // because constant integers outside 2^63 are rare enough
        // that there is no performance impact
        mv.visitTypeInsn(NEW, "java/math/BigInteger");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(a1.toString(Character.MAX_RADIX));
        mv.visitIntInsn(BIPUSH, Character.MAX_RADIX);
        mv.visitMethodInsn(
            INVOKESPECIAL, "java/math/BigInteger", "<init>", "(Ljava/lang/String;I)V", false);
      }
      return;
    }
    if (a instanceof Sym) {
      mv.visitLdcInsn(a.toString());
      mv.visitMethodInsn(
          INVOKESTATIC, "aklo/Sym", "intern", "(Ljava/lang/String;)Laklo/Sym;", false);
      return;
    }
    if (a instanceof Boolean a1) {
      mv.visitFieldInsn(
          GETSTATIC, "java/lang/Boolean", a1 ? "TRUE" : "FALSE", "Ljava/lang/Boolean;");
      return;
    }
    if (a instanceof BigRational a1) {
      throw new UnsupportedOperationException(a.toString());
    }

    // list
    if (a instanceof List a1) {
      var n = a1.size();
      if (n <= 10) {
        for (var b : a1) load(mv, b);
        mv.visitMethodInsn(
            INVOKESTATIC,
            "java/util/List",
            "of",
            '(' + "Ljava/lang/Object;".repeat(n) + ")Ljava/util/List;",
            true);
        return;
      }
      emitInt(mv, n);
      mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
      for (var i = 0; i < n; i++) {
        mv.visitInsn(DUP);
        emitInt(mv, i);
        load(mv, a1.get(i));
        mv.visitInsn(AASTORE);
      }
      mv.visitMethodInsn(
          INVOKESTATIC,
          "java/util/Arrays",
          "asList",
          "([Ljava/lang/Object;)Ljava/util/List;",
          false);
      return;
    }

    // floating point is directly supported apart from the conversion to object reference
    mv.visitLdcInsn(a);
    if (a instanceof Float) {
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
      return;
    }
    assert a instanceof Double;
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
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
    return Type.INT;
  }
}
