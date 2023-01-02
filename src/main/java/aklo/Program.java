package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
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

    // functions
    for (var f : fns) {
      f.dbg();

      // assign local variables
      var i = 0;
      for (var block : f.blocks)
        for (var a : block.insns)
          switch (a.type().kind()) {
            case VOID -> {}
            case DOUBLE -> {
              // TODO long
              a.localVar = i;
              i += 2;
            }
            default -> a.localVar = i++;
          }

      // emit code
      var mv = w.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      for (var block : f.blocks)
        for (var a : block.insns) {
          a.emit(mv);
          switch (a.type().kind()) {
            case VOID -> {}
            case DOUBLE -> mv.visitVarInsn(DSTORE, a.localVar);
            default -> mv.visitVarInsn(ASTORE, a.localVar);
          }
        }
      mv.visitInsn(RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
    w.visitEnd();

    // write class file
    Files.write(Path.of("a.class"), w.toByteArray());
  }
}
