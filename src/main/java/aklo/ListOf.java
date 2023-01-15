package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;

final class ListOf extends Nary {
  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    var n = size();
    if (n <= 10) {
      for (var a : this) a.load(, mv);
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
      get(i).load(, mv);
      mv.visitInsn(AASTORE);
    }
    mv.visitMethodInsn(
        INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false);
  }

  ListOf(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  static ListOf encode(Loc loc, String s) {
    return of(loc, s.getBytes(StandardCharsets.UTF_8));
  }

  static ListOf of(Loc loc, byte[] s) {
    var terms = new Term[s.length];
    for (var i = 0; i < s.length; i++) terms[i] = new Const(loc, BigInteger.valueOf(s[i] & 0xff));
    return new ListOf(loc, terms);
  }

  ListOf(Loc loc, Term[] terms) {
    // TODO do we need two constructors?
    super(loc, terms);
  }

  @Override
  Type type() {
    return Type.ANY;
  }

  @Override
  Tag tag() {
    return Tag.LIST_OF;
  }
}
