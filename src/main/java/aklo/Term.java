package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;
import java.util.function.Consumer;
import org.objectweb.asm.MethodVisitor;

abstract class Term extends AbstractCollection<Term> {
  final Loc loc;

  Term(Loc loc) {
    this.loc = loc;
  }

  abstract Tag tag();

  boolean isTerminator() {
    return false;
  }

  static void emitInt(MethodVisitor mv, int n) {
    // TODO
    mv.visitIntInsn(BIPUSH, n);
  }

  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    throw new UnsupportedOperationException(String.format("%s: %s", loc, this));
  }

  @Override
  public String toString() {
    return tag().name().toLowerCase(Locale.ROOT);
  }

  void dbg(Map<Object, Integer> refs) {
    System.out.print(this);
    for (var i = 0; i < size(); i++) dbg(refs, get(i));
  }

  void dbg(Map<Object, Integer> refs, Term a) {
    System.out.print(' ');
    var j = refs.get(a);
    if (j == null) System.out.print(a);
    else System.out.print("%" + j);
  }

  final void walk(Consumer<Term> f) {
    f.accept(this);
    for (var a : this) a.walk(f);
  }

  void set(int i, Term a) {
    throw new UnsupportedOperationException(toString());
  }

  Type type() {
    return Type.VOID;
  }

  void load(Map<Object, Integer> refs, MethodVisitor mv) {
    var i = refs.get(this);
    if (i == null) throw new IllegalStateException(String.format("%s: %s", loc, this));
    // TODO
    switch (type().kind()) {
      case VOID -> throw new UnsupportedOperationException(toString());
        // case DOUBLE -> mv.visitVarInsn(DLOAD, localVar);
      default -> mv.visitVarInsn(ALOAD, i);
    }
  }

  Term get(int i) {
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
