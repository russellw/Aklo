package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

final class GetStatic extends Term {
  final String owner;
  final String name;
  final String descriptor;

  @Override
  public String toString() {
    return String.format("getstatic(\"%s\", \"%s\", \"%s\")", owner, name, descriptor);
  }

  @Override
  void emit(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, owner, name, descriptor);
  }

  GetStatic(Loc loc, String owner, String name, String descriptor) {
    super(loc);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  Tag tag() {
    return Tag.GET_STATIC;
  }
}
