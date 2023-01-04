package aklo;

import static org.objectweb.asm.Opcodes.ASTORE;

import org.objectweb.asm.MethodVisitor;

public final class Assign extends Term2 {
  public Assign(Loc loc, Term arg0, Term arg1) {
    super(loc, arg0, arg1);
  }

  @Override
  public void emit(MethodVisitor mv) {
    arg1.load(mv);
    if (arg0.localVar < 0) throw new IllegalStateException(str());
    mv.visitVarInsn(ASTORE, arg0.localVar);
  }

  @Override
  public Tag tag() {
    return Tag.ASSIGN;
  }
}
