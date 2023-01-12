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

  private static final class LinkLocals {
    final LinkLocals outer;
    final Map<String, Term> locals = new HashMap<>();

    Term get(String name) {
      for (var l = this; ; l = l.outer) {
        if (l == null) return null;
        var r = l.locals.get(name);
        if (r != null) return r;
      }
    }

    LinkLocals(LinkLocals outer, Fn f) {
      this.outer = outer;
      for (var x : f.vars) if (x.name != null) locals.put(x.name, x);
      for (var block : f.blocks)
        for (var a : block.insns)
          for (var i = 0; i < a.size(); i++)
            if (a.get(i) instanceof Id id) {
              var x = get(id.name);
              if (x == null) throw new CompileError(a.loc, id + " not found");
              a.set(i, x);
            }
    }
  }

  public Program(Map<List<String>, Fn> modules) {
    for (var module : modules.values()) {
      new LinkLocals(null, module);
      fns.add(module);
    }
  }

  public void write() throws IOException {
    var w = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    w.visit(V17, ACC_PUBLIC, "a", null, "java/lang/Object", new String[0]);

    // functions
    for (var f : fns) {
      f.dbg();

      // label blocks
      for (var block : f.blocks) block.label = new Label();

      // assign local variable numbers
      var i = 0;
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
      var mv = w.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
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
