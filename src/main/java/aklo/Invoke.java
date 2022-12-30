package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import java.util.Map;

public final class Invoke extends Terms {
  public final int opcode;
  public final String owner;
  public final String name;
  public final String descriptor;

  @Override
  void dbg(Map<Term, Integer> refs) {
    System.out.print("invoke");
    switch (opcode) {
      case INVOKESTATIC -> System.out.print("static");
      case INVOKEVIRTUAL -> System.out.print("virtual");
      case INVOKESPECIAL -> System.out.print("special");
      default -> throw new IllegalStateException(Integer.toString(opcode));
    }
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
