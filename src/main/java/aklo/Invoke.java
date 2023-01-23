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
  public String toString() {
    var sb = new StringBuilder("Invoke");
    sb.append(
        switch (opcode) {
          case INVOKESTATIC -> "Static";
          case INVOKEVIRTUAL -> "Virtual";
          case INVOKESPECIAL -> "Special";
          default -> throw new IllegalStateException(Integer.toString(opcode));
        });
    sb.append(' ');
    sb.append(owner);
    sb.append(' ');
    sb.append(name);
    sb.append(' ');
    sb.append(descriptor);
    return sb.toString();
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
