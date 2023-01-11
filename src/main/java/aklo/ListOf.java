package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.objectweb.asm.MethodVisitor;

public final class ListOf extends Terms {
  @Override
  public void emit(MethodVisitor mv) {
    var n = size();
    if (n <= 10) {
      for (var a : this) a.load(mv);
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
      get(i).load(mv);
      mv.visitInsn(AASTORE);
    }
    mv.visitMethodInsn(
        INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false);
  }

  public ListOf(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  public static ListOf encode(Loc loc, String s) {
    return of(loc, s.getBytes(StandardCharsets.UTF_8));
  }

  public static ListOf of(Loc loc, byte[] s) {
    var terms = new Term[s.length];
    for (var i = 0; i < s.length; i++) terms[i] = new Const(loc, BigInteger.valueOf(s[i] & 0xff));
    return new ListOf(loc, terms);
  }

  public ListOf(Loc loc, Term[] terms) {
    // TODO do we need two constructors?
    super(loc, terms);
  }

  @Override
  public Type type() {
    return Type.ANY;
  }

  @Override
  public Tag tag() {
    return Tag.LIST_OF;
  }
}
