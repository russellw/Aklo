package aklo;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

final class Main {
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
    load(file, List.of("aklo", name), readResource(file));
  }

  private static void load(String file, List<String> names, byte[] text) {
    Link.modules.put(names, Parser.parse(file, names.get(names.size() - 1), text));
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
            var file = path.toString();

            // module name runs from the package root to the file
            var names = new ArrayList<String>();
            for (var j = i; j < path.getNameCount(); j++)
              names.add(withoutExt(path.getName(j).toString()));

            // load the module
            load(file, names, Files.readAllBytes(Path.of(file)));
          }
        }
      }
      loadResource("ubiquitous");

      // resolve names to variables and functions
      Link.link();

      // convert to basic blocks
      Program.init(Link.modules.values());
      Verifier.verify();

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
