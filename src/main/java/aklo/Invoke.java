package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

public final class Invoke extends Terms {
  public final int opcode;
  public final String owner;
  public final String name;
  public final String descriptor;

  @Override
  public void emit(MethodVisitor mv) {
    mv.visitMethodInsn(opcode, owner, name, descriptor, false);
  }

  @Override
  void dbg(Map<Term, Integer> refs) {
    System.out.print("invoke");
    System.out.print(
        switch (opcode) {
          case INVOKESTATIC -> "static";
          case INVOKEVIRTUAL -> "virtual";
          case INVOKESPECIAL -> "special";
          default -> throw new IllegalStateException(Integer.toString(opcode));
        });
    System.out.printf(" \"%s\", \"%s\", \"%s\"", owner, name, descriptor);
    for (var a : this) System.out.print(", " + refs.get(a));
  }

  public Invoke(
      Loc loc, int opcode, String owner, String name, String descriptor, List<Term> terms) {
    super(loc, terms);
    this.opcode = opcode;
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public Tag tag() {
    return Tag.INVOKE;
  }

  @Override
  public Term remake(Loc loc, Term[] terms) {
    return null;
  }
}
