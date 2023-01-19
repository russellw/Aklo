package aklo;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

final class Main {
  private static String file;
  private static final Map<List<String>, Fn> namesModules = new HashMap<>();
  private static final List<Fn> modules = new ArrayList<>();
  private static final Map<Fn, String> moduleFiles = new HashMap<>();

  private Main() {}

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
          if (x == null) throw new CompileError(file, line, name + " not found");
          a.set(i, x);
        }
      if (a instanceof Assign && a.get(0) instanceof Fn)
        throw new CompileError(file, line, a.get(0) + ": assigning a function");
      if (a instanceof Line a1) line = a1.line;
    }

    Link(Link outer, Fn f) {
      this.outer = outer;
      for (var g : f.fns) {
        new Link(this, g);
        locals.put(g.name, g);
      }
      for (var x : f.params) locals.put(x.name, x);
      for (var x : f.vars) locals.put(x.name, x);
      for (var block : f.blocks) for (var a : block.instructions) link(a);
    }
  }

  private static void options() {
    System.out.println("-h  Show help");
    System.out.println("-V  Show version");
  }

  private static String version() throws IOException {
    var properties = new Properties();
    try (var stream = Main.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {
      properties.load(stream);
      return properties.getProperty("Implementation-Version");
    }
  }

  private static void printVersion() throws IOException {
    System.out.printf("Aklo %s, %s\n", version(), System.getProperty("java.class.path"));
    System.out.printf(
        "%s, %s, %s\n",
        System.getProperty("java.vm.name"),
        System.getProperty("java.vm.version"),
        System.getProperty("java.home"));
    System.out.printf(
        "%s, %s, %s\n",
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("os.arch"));
  }

  private static String withoutExt(String file) {
    var i = file.indexOf('.');
    if (i < 0) return file;
    return file.substring(0, i);
  }

  private static byte[] readResource(String file) throws IOException {
    try (var stream = Main.class.getClassLoader().getResourceAsStream(file)) {
      //noinspection ConstantConditions
      return stream.readAllBytes();
    }
  }

  private static void loadResource(String name) throws IOException {
    var file = name + ".k";
    load(file, name, readResource(file));
  }

  private static Fn load(String file, String name, byte[] text) {
    var module = Parser.parse(file, name, text);
    modules.add(module);
    moduleFiles.put(module, file);
    return module;
  }

  public static void main(String[] args) throws IOException {
    // command line
    if (args.length == 0) {
      System.err.println("Usage: aklo [options] packages");
      System.exit(1);
    }
    var packages = new ArrayList<Path>();
    for (var arg : args) {
      var s = arg;
      if (s.startsWith("-")) {
        while (s.startsWith("-")) s = s.substring(1);
        if (s.isEmpty()) {
          options();
          System.exit(1);
        }
        switch (s.charAt(0)) {
          case 'h' -> {
            options();
            return;
          }
          case 'V' -> {
            printVersion();
            return;
          }
        }
        System.err.println(arg + ": unknown option");
        System.exit(1);
      }
      packages.add(Path.of(s));
    }

    try {
      // parse
      for (var p : packages) {
        var i = p.getNameCount() - 1;
        try (var files = Files.walk(p)) {
          for (var path :
              files.filter(path -> path.toString().endsWith(".k")).toArray(Path[]::new)) {
            file = path.toString();

            // module name runs from the package root to the file
            var names = new ArrayList<String>();
            for (var j = i; j < path.getNameCount(); j++)
              names.add(withoutExt(path.getName(j).toString()));

            // load the module
            var module = load(file, names.get(names.size() - 1), Files.readAllBytes(Path.of(file)));
            namesModules.put(names, module);
          }
        }
      }
      // loadResource("ubiquitous");

      // resolve names to variables and functions
      for (var module : modules) {
        file = moduleFiles.get(module);
        new Link(null, module);
      }

      // convert to basic blocks
      Program.init(modules);

      // optimize
      Optimizer.optimize();

      // write class file
      Program.write();
    } catch (CompileError e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
