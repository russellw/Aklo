package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

public final class Program {
  private Program() {}

  public static final List<Var> vars = new ArrayList<>();
  public static final List<Fn> fns = new ArrayList<>();

  private static final class Link {
    final Link outer;
    final Map<String, Term> locals = new HashMap<>();

    Term get(String name) {
      for (var l = this; ; l = l.outer) {
        if (l == null) return null;
        var r = l.locals.get(name);
        if (r != null) return r;
      }
    }

    @SuppressWarnings("ConstantConditions")
    void link(Term a) {
      for (var i = 0; i < a.size(); i++)
        if (a.get(i) instanceof Id id) {
          var x = get(id.name);
          if (x == null) throw new CompileError(a.loc, id + " not found");
          a.set(i, x);
        }
      if (a instanceof Assign && a.get(0) instanceof Fn)
        throw new CompileError(a.loc, a.get(0) + ": assigning a function");
    }

    Link(Link outer, Fn f) {
      this.outer = outer;
      for (var g : f.fns) {
        new Link(this, g);
        if (g.name != null) locals.put(g.name, g);
      }
      for (var x : f.params) if (x.name != null) locals.put(x.name, x);
      for (var x : f.vars) if (x.name != null) locals.put(x.name, x);
      for (var block : f.blocks) for (var a : block.insns) link(a);
      fns.add(f);
    }
  }

  public static void init(Map<List<String>, Fn> modules) {
    var main = new Fn(null, "main");
    var args = new Var(main.params);
    args.type = new ArrayType(Type.STRING);
    main.rtype = Type.VOID;
    for (var module : modules.values()) {
      // resolve names to variables and functions
      new Link(null, module);

      // Module scope variables are static
      vars.addAll(module.vars);
      module.vars.clear();

      // modules may contain initialization code
      // so each module is called as a function from main
      main.blocks.get(0).insns.add(new Call(module.loc, List.of(module)));
    }
    main.blocks.get(0).insns.add(new ReturnVoid(main.loc));
    fns.add(main);
  }

  public static void write() throws IOException {
    var w = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    w.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);

    // functions
    // TODO factor out into Fn method?
    for (var f : fns) {
      // label blocks
      for (var block : f.blocks) block.label = new Label();

      // assign local variable numbers
      var i = 0;
      for (var x : f.params) x.localVar = i++;
      for (var x : f.vars) x.localVar = i++;
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
      var mv = w.visitMethod(ACC_PUBLIC | ACC_STATIC, f.name, f.descriptor(), null, null);
      mv.visitCode();
      for (var block : f.blocks) {
        mv.visitLabel(block.label);
        for (var a : block.insns) {
          a.emit(mv);
          switch (a.type().kind()) {
            case VOID -> {}
              // case DOUBLE -> mv.visitVarInsn(DSTORE, a.localVar);
            default -> mv.visitVarInsn(ASTORE, a.localVar);
          }
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
