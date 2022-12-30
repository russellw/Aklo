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

    // represent nontrivial constants as static final fields
    var consts = new HashMap<Object, Var>();
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
                      vars.add(x);
                    }
                  }
                }
              });

    // functions
    for (var f : fns) {
      var v = w.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      v.visitCode();

      v.visitInsn(RETURN);
      v.visitMaxs(0, 0);
      v.visitEnd();
    }
    w.visitEnd();

    // write class file
    Files.write(Path.of("a.class"), w.toByteArray());
  }
}
