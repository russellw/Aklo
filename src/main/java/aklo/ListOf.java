package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class ListOf extends Nary {
  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    var n = size();
    if (n <= 10) {
      for (var a : this) load(refs, mv, a);
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
      load(refs, mv, get(i));
      mv.visitInsn(AASTORE);
    }
    mv.visitMethodInsn(
        INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false);
  }

  static ListOf encode(Loc loc, String s) {
    // TODO
    return of(loc, s.getBytes(StandardCharsets.UTF_8));
  }

  static ListOf of(Loc loc, byte[] s) {
    var r = new Object[s.length];
    for (var i = 0; i < s.length; i++) r[i] = BigInteger.valueOf(s[i] & 0xff);
    return new ListOf(loc, r);
  }

  ListOf(Loc loc, Object... args) {
    super(loc, args);
  }

  @Override
  String type() {
    return "Ljava/util/List;";
  }
}
