package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Invoke extends Nary {
  final int opcode;
  final String owner;
  final String name;
  final String descriptor;

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {
    for (var a : this) load(refs, mv, a);
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

  Invoke(int opcode, String owner, String name, String descriptor, Object... args) {
    super(args);
    this.opcode = opcode;
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  String type() {
    var i = descriptor.lastIndexOf(')');
    return descriptor.substring(i + 1);
  }
}
