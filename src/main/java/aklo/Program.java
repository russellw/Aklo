package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.objectweb.asm.ClassWriter;

final class Program {
  private Program() {}

  static final List<Var> vars = new ArrayList<>();
  static final List<Fn> fns = new ArrayList<>();

  private static final class Link {
    final Link outer;
    final Map<String, Object> locals = new HashMap<>();
    int line;

    Object get(String name) {
      for (var l = this; ; l = l.outer) {
        if (l == null) return null;
        var r = l.locals.get(name);
        if (r != null) return r;
      }
    }

    @SuppressWarnings("ConstantConditions")
    void link(Instruction a) {
      for (var i = 0; i < a.size(); i++)
        if (a.get(i) instanceof String name) {
          var x = get(name);
          if (x == null) throw new CompileError(a.loc.file(), line, name + " not found");
          a.set(i, x);
        }
      if (a instanceof Assign && a.get(0) instanceof Fn)
        throw new CompileError(a.loc.file(), line, a.get(0) + ": assigning a function");
      if (a instanceof Line a1) line = a1.line;
    }

    Link(Link outer, Fn f) {
      this.outer = outer;
      for (var g : f.fns) {
        new Link(this, g);
        if (g.name != null) locals.put(g.name, g);
      }
      for (var x : f.params) if (x.name != null) locals.put(x.name, x);
      for (var x : f.vars) if (x.name != null) locals.put(x.name, x);
      for (var block : f.blocks) for (var a : block.instructions) link(a);
      fns.add(f);
    }
  }

  static void init(Map<List<String>, Fn> modules) {
    var main = new Fn("main");
    var args = new Var("args", main.params);
    args.type = "[Ljava/lang/String;";
    main.rtype = "V";
    for (var module : modules.values()) {
      // resolve names to variables and functions
      new Link(null, module);

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
    var i = 0;
    for (var x : vars) {
      if (x.name.endsWith("$")) x.name += i++;
      w.visitField(ACC_STATIC, x.name, x.type, null, null).visitEnd();
    }

    // functions
    for (var f : fns) f.write(w);

    // write class file
    w.visitEnd();
    Files.write(Path.of("a.class"), w.toByteArray());
  }
}
