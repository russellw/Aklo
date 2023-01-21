package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.objectweb.asm.ClassWriter;

final class Program {
  static final List<Var> vars = new ArrayList<>();
  static final List<Fn> fns = new ArrayList<>();

  private static void lift(Fn f) {
    for (var g : f.fns) lift(g);
    fns.add(f);
  }

  static void init(Collection<Fn> modules) {
    var main = new Fn("main");
    var args = new Var("args", main.params);
    args.type = "[Ljava/lang/String;";
    main.rtype = "V";
    for (var module : modules) {
      // lift functions to global scope
      lift(module);

      // Module scope variables are static
      vars.addAll(module.vars);
      module.vars.clear();

      // modules may contain initialization code
      // so each module is called as a function from main
      main.lastBlock().instructions.add(new Call(module));
    }
    main.lastBlock().instructions.add(new ReturnVoid());
    fns.add(main);
  }

  static void write() throws IOException {
    var w = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    w.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);

    // global variables
    Named.unique(vars);
    for (var x : vars) w.visitField(ACC_STATIC, x.name, x.type, null, null).visitEnd();

    // functions
    Named.unique(fns);
    for (var f : fns) f.write(w);

    // write class file
    w.visitEnd();
    Files.write(Path.of("a.class"), w.toByteArray());
  }
}
