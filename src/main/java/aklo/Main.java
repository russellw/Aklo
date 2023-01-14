package aklo;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

final class Main {
  private Main() {}

  private static void options() {
    System.out.println("-h  Show help");
    System.out.println("-V  Show version");
  }

  private static String version() {
    var properties = new Properties();
    var stream =
        Main.class.getClassLoader().getResourceAsStream("META-INF/maven/aklo/aklo/pom.properties");
    if (stream == null) return null;
    try {
      properties.load(stream);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return properties.getProperty("version");
  }

  private static void printVersion() {
    System.out.printf(
        "Aklo %s, %s\n",
        Objects.toString(version(), "[unknown version, not running from jar]"),
        System.getProperty("java.class.path"));
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

    // modules may contain initialization code
    // that is guaranteed to be run in deterministic order
    // so make sure the collection of modules keeps deterministic order
    var modules = new LinkedHashMap<List<String>, Fn>();

    // parse
    for (var p : packages) {
      var i = p.getNameCount() - 1;
      try (var files = Files.walk(p)) {
        for (var path : files.filter(path -> path.toString().endsWith(".k")).toArray(Path[]::new)) {
          var file = path.toString();
          var loc = new Loc(file, 1);

          // module name runs from the package root to the file
          var names = new ArrayList<String>();
          for (var j = i; j < path.getNameCount(); j++)
            names.add(withoutExt(path.getName(j).toString()));

          // parse the module
          var module = new Fn(loc, names.get(names.size() - 1));
          module.rtype = Type.VOID;
          new Parser(file, Files.readAllBytes(Path.of(file)), module);
          modules.put(names, module);
        }
      }
    }

    // convert to basic blocks
    var program = new Program(modules);

    // write class file
    program.write();
  }
}
