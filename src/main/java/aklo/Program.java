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
    for (var module : modules) module.toBlocks();
  }

  public void write() throws IOException {
    ClassWriter cw = new ClassWriter(0);
    cw.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);
    cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "(Ljava/lang/String;)V", null, null).visitEnd();
    cw.visitEnd();
    Files.write(Path.of("a.class"), cw.toByteArray());
  }
}
