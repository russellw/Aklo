package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Invoke extends Nary {
  final int opcode;
  final String owner;
  final String name;
  final String descriptor;

  @Override
  void emit(MethodVisitor mv) {
    for (var a : this) a.load(mv);
    mv.visitMethodInsn(opcode, owner, name, descriptor, false);
  }

  @Override
  void dbg(Map<Object, Integer> refs) {
    System.out.print("invoke");
    System.out.print(
        switch (opcode) {
          case INVOKESTATIC -> "static";
          case INVOKEVIRTUAL -> "virtual";
          case INVOKESPECIAL -> "special";
          default -> throw new IllegalStateException(Integer.toString(opcode));
        });
    System.out.printf(" \"%s\" \"%s\" \"%s\"", owner, name, descriptor);
    for (var a : this) dbg(refs, a);
  }

  Invoke(Loc loc, int opcode, String owner, String name, String descriptor, List<Term> terms) {
    super(loc, terms);
    this.opcode = opcode;
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  Invoke(Loc loc, int opcode, String owner, String name, String descriptor, Term... terms) {
    super(loc, terms);
    this.opcode = opcode;
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  Type type() {
    var i = descriptor.lastIndexOf(')');
    // TODO
    // return Type.of(descriptor.substring(i + 1));
    return descriptor.endsWith("V") ? Type.VOID : Type.ANY;
  }

  @Override
  Tag tag() {
    return Tag.INVOKE;
  }
}
