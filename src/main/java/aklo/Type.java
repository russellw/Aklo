package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassWriter;

final class Type extends Named {
  // the classes of the program being compiled, excluding system types
  static Type main;
  static final List<Type> classes = new ArrayList<>();

  // system types
  static final Type INT = new Type("I");

  final List<Var> vars = new ArrayList<>();
  final List<Fn> fns = new ArrayList<>();

  Type(String name) {
    super(name);
  }

  static void writeClasses() throws IOException {
    Named.unique(classes);
    for (var c : classes) c.write();
  }

  private void write() throws IOException {
    var w = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    w.visit(V17, ACC_PUBLIC, name, null, "java/lang/Object", new String[0]);

    // static variables
    Named.unique(vars);
    for (var x : vars) w.visitField(ACC_STATIC, x.name, x.type, null, null).visitEnd();

    // functions
    Named.unique(fns);
    for (var f : fns) f.write(w);

    // write class file
    w.visitEnd();
    Files.write(Path.of(name + ".class"), w.toByteArray());
  }
}
