package aklo;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public final class Get_static extends Term {
  public final String owner;
  public final String name;
  public final String descriptor;

  @Override
  public String toString() {
    return String.format("getstatic(\"%s\", \"%s\", \"%s\")", owner, name, descriptor);
  }

  @Override
  public void emit(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, owner, name, descriptor);
  }

  public Get_static(Loc loc, String owner, String name, String descriptor) {
    super(loc);
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public Tag tag() {
    return Tag.GET_STATIC;
  }
}
