package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;
import java.util.function.Consumer;
import org.objectweb.asm.MethodVisitor;

public abstract class Term extends AbstractCollection<Term> {
  public final Loc loc;
  public int localVar = -1;

  public Term(Loc loc) {
    this.loc = loc;
  }

  public abstract Tag tag();

  public boolean isTerminator() {
    return false;
  }

  public static void emitInt(MethodVisitor mv, int n) {
    // TODO
    mv.visitIntInsn(BIPUSH, n);
  }

  public void emit(MethodVisitor mv) {
    throw new UnsupportedOperationException(str());
  }

  @Override
  public String toString() {
    return tag().name().toLowerCase(Locale.ROOT);
  }

  public final String str() {
    var s = toString();
    if (isEmpty()) return s;
    var sb = new StringBuilder(s + '(');
    for (var i = 0; i < size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(get(i).str());
    }
    sb.append(')');
    return sb.toString();
  }

  public void dbg(Map<Term, Integer> refs) {
    System.out.print(this);
    for (var i = 0; i < size(); i++) {
      if (i > 0) System.out.print(',');
      System.out.print(' ');
      var a = get(i);
      var j = refs.get(a);
      if (j == null) System.out.print(a);
      else System.out.print("%" + j);
    }
  }

  public final void walk(Consumer<Term> f) {
    f.accept(this);
    for (var a : this) a.walk(f);
  }

  public void set(int i, Term a) {
    throw new UnsupportedOperationException(toString());
  }

  public Type type() {
    return Type.VOID;
  }

  public void load(MethodVisitor mv) {
    if (localVar < 0) throw new IllegalStateException(toString());
    // TODO
    switch (type().kind()) {
      case VOID -> throw new UnsupportedOperationException(toString());
        // case DOUBLE -> mv.visitVarInsn(DLOAD, localVar);
      default -> mv.visitVarInsn(ALOAD, localVar);
    }
  }

  public Term get(int i) {
    throw new UnsupportedOperationException(toString());
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Iterator<Term> iterator() {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public Term next() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
