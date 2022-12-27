package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

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
    ClassWriter cw = new ClassWriter(0);
    cw.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);
    cw.visitSource("a.java", null);

    for (var f : fns) {
      Etc.dbg(f.blocks);
      var mv =
          cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.visitLineNumber(2, label0);
      mv.visitInsn(RETURN);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }

    cw.visitEnd();
    Files.write(Path.of("a.class"), cw.toByteArray());
  }
}
