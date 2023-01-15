package aklo;

import static org.objectweb.asm.Opcodes.ASTORE;

import org.objectweb.asm.MethodVisitor;

final class Assign extends Binary {
  Assign(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  Type type() {
    return Type.VOID;
  }

  @Override
  void emit(MethodVisitor mv) {
    arg1.load(mv);
    if (arg0.localVar < 0) throw new IllegalStateException(str());
    mv.visitVarInsn(ASTORE, arg0.localVar);
  }

  @Override
  Tag tag() {
    return Tag.ASSIGN;
  }
}
