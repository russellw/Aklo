package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

abstract class Instruction extends AbstractCollection<Object> {
  boolean isTerminator() {
    return false;
  }

  static void emitInt(MethodVisitor mv, int n) {
    // TODO
    mv.visitIntInsn(BIPUSH, n);
  }

  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    throw new UnsupportedOperationException(toString());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  void dbg(Map<Object, Integer> refs) {
    System.out.print(this);
    for (var i = 0; i < size(); i++) dbg(refs, get(i));
  }

  static void dbg(Map<Object, Integer> refs, Object a) {
    System.out.print(' ');
    var j = refs.get(a);
    if (j == null) System.out.print(a);
    else System.out.print("%" + j);
  }

  static void walk(Object a, Consumer<Object> f) {
    f.accept(a);
    if (a instanceof Instruction a1) for (var b : a1) walk(b, f);
  }

  void set(int i, Object a) {
    throw new UnsupportedOperationException(toString());
  }

  String type() {
    return "V";
  }

  static void load(Map<Object, Integer> refs, MethodVisitor mv, Object a) {
    var i = refs.get(a);
    if (i != null) {
      mv.visitVarInsn(ALOAD, i);
      return;
    }

    if (a instanceof Var a1) {
      mv.visitFieldInsn(GETSTATIC, "a", a1.name, a1.type);
      return;
    }
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
    if (a instanceof List a1) {
      var n = a1.size();
      if (n <= 10) {
        for (var b : a1) load(refs, mv, b);
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
      for (var j = 0; j < n; j++) {
        mv.visitInsn(DUP);
        emitInt(mv, j);
        load(refs, mv, a1.get(j));
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
    if (a instanceof Float) {
      mv.visitLdcInsn(a);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
      return;
    }
    if (a instanceof Double) {
      mv.visitLdcInsn(a);
      mv.visitMethodInsn(
          INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
      return;
    }
    if (a instanceof Fn) {
      mv.visitInvokeDynamicInsn(
          "apply",
          "()Ljava/util/function/UnaryOperator;",
          new Handle(
              Opcodes.H_INVOKESTATIC,
              "java/lang/invoke/LambdaMetafactory",
              "metafactory",
              "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
              false),
          Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
          new Handle(
              Opcodes.H_INVOKESTATIC,
              "a",
              a.toString(),
              "(Ljava/lang/Object;)Ljava/lang/Object;",
              false),
          Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"));
      return;
    }
    throw new IllegalArgumentException(a.toString());
  }

  Object get(int i) {
    throw new UnsupportedOperationException(toString());
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Iterator<Object> iterator() {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public Object next() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
