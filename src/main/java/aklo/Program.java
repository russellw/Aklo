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
    var w = new ClassWriter(0);
    w.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);
    w.visitSource("a.java", null);
    for (var f : fns) {
      var v = w.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      v.visitCode();

      v.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "()V", false);

      v.visitInsn(RETURN);
      v.visitMaxs(1, 1);
      v.visitEnd();
    }
    w.visitEnd();
    Files.write(Path.of("a.class"), w.toByteArray());
  }
}
