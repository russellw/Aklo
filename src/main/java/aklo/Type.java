package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.objectweb.asm.ClassWriter;

class Type extends Named {
  // system types
  static final Type VOID = new Type("V");
  static final Type BOOL_PRIM = new Type("Z");
  static final Type FLOAT_PRIM = new Type("F");
  static final Type DOUBLE_PRIM = new Type("D");
  static final Type INTEGER = new Type("java/math/BigInteger");
  static final Type RATIONAL = new Type("aklo/BigRational");
  static final Type SYM = new Type("aklo/Sym");
  static final Type LIST = new Type("java/util/List");
  static final Type STRING = new Type("java/lang/String");
  static final Type BOOL = new Type("java/lang/Boolean");
  static final Type FLOAT = new Type("java/lang/Float");
  static final Type DOUBLE = new Type("java/lang/Double");
  static final Type OBJECT = new Type("java/lang/Object");

  // the classes of the program being compiled, excluding system types
  static final Type mainClass = new Type("a");
  static final List<Type> classes = new ArrayList<>(List.of(mainClass));
  static Fn mainFn;

  final List<Var> vars = new ArrayList<>();
  final List<Fn> fns = new ArrayList<>();

  @Override
  public String toString() {
    if (name.length() == 1) return name;
    return 'L' + name + ';';
  }

  Type(String name) {
    super(name);
  }

  private static void lift(Fn f) {
    for (var g : f.fns) lift(g);
    mainClass.fns.add(f);
  }

  static void init(Collection<Fn> modules) {
    mainFn = new Fn("main");
    var args = new Var("args", mainFn.params);
    args.type = Array.of(STRING);
    mainFn.rtype = VOID;
    for (var module : modules) {
      // lift functions to global scope
      lift(module);

      // Module scope variables are static
      mainClass.vars.addAll(module.vars);
      module.vars.clear();

      // modules may contain initialization code
      // so each module is called as a function from main
      mainFn.lastBlock().instructions.add(new Call(module));
    }
    mainFn.lastBlock().instructions.add(new ReturnVoid());
    mainClass.fns.add(mainFn);
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
    for (var x : vars) w.visitField(ACC_STATIC, x.name, x.type.toString(), null, null).visitEnd();

    // functions
    Named.unique(fns);
    for (var f : fns) f.write(w);

    // write class file
    w.visitEnd();
    Files.write(Path.of(name + ".class"), w.toByteArray());
  }

  public static Type of(String descriptor) {
    return switch (descriptor) {
      case "V" -> VOID;
      case "F" -> FLOAT_PRIM;
      case "D" -> DOUBLE_PRIM;
      case "Z" -> BOOL_PRIM;
      case "Laklo/BigRational;" -> RATIONAL;
      case "Laklo/Sym;" -> SYM;
      case "Ljava/math/BigInteger;" -> INTEGER;
      case "Ljava/util/List;" -> LIST;
      case "Ljava/lang/Boolean;" -> BOOL;
      default -> {
        if (descriptor.startsWith("Ljava/util/ImmutableCollections$List")) yield LIST;
        throw new IllegalArgumentException(descriptor);
      }
    };
  }
}
