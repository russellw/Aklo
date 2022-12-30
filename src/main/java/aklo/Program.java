package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.objectweb.asm.ClassWriter;

public final class Program {
  public final List<Var> vars = new ArrayList<>();
  public final List<Fn> fns = new ArrayList<>();

  public Program(List<Module> modules) {
    for (var module : modules) {
      module.toBlocks();
      fns.add(module);
    }
  }

  public void write() throws IOException {
    var w = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    w.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);

    // represent nontrivial constants as static final fields
    var consts = new LinkedHashMap<Object, Var>();
    for (var f : fns)
      for (var block : f.blocks)
        for (var a : block.insns)
          a.walk(
              b -> {
                switch (b.tag()) {
                  case INTEGER, RATIONAL -> {
                    var val = b.val();
                    var x = consts.get(val);
                    if (x == null) {
                      x = new Var(b.loc, '$' + Integer.toString(consts.size()));
                      x.type = b.type();
                      x.val = val;
                      consts.put(val, x);

                      var fv =
                          w.visitField(
                              ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                              x.name,
                              x.type.descriptor(),
                              null,
                              null);
                      fv.visitEnd();
                    }
                  }
                }
              });
    var mv = w.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    for (var x : consts.values()) {
      switch (x.type.kind()) {
        case INTEGER -> {
          var val = (BigInteger) x.val;
          try {
            mv.visitLdcInsn(val.longValueExact());
            mv.visitMethodInsn(
                INVOKESTATIC,
                "java/math/BigInteger",
                "valueOf",
                "(J)Ljava/math/BigInteger;",
                false);
          } catch (ArithmeticException e) {
            // okay to use an exception for something that is not an error here
            // because constant integers outside 2^63 are rare enough
            // that there is no performance impact
            mv.visitTypeInsn(NEW, "java/math/BigInteger");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(val.toString(16));
            mv.visitIntInsn(BIPUSH, 16);
            mv.visitMethodInsn(
                INVOKESPECIAL, "java/math/BigInteger", "<init>", "(Ljava/lang/String;I)V", false);
          }
          mv.visitFieldInsn(PUTSTATIC, "a", x.name, "Ljava/math/BigInteger;");
        }
        default -> throw new IllegalStateException(x.toString());
      }
      mv.visitInsn(RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }

    // functions
    for (var f : fns) {
      f.dbg();
      mv = w.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      for (var block : f.blocks) {}

      mv.visitInsn(RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
    w.visitEnd();

    // write class file
    Files.write(Path.of("a.class"), w.toByteArray());
  }
}
