package aklo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

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

    // parse
    var modules = new ArrayList<Module>();
    for (var p : packages) {
      var i = p.getNameCount() - 1;
      try (var files = Files.walk(p)) {
        for (var path : files.filter(path -> path.toString().endsWith(".k")).toArray(Path[]::new)) {
          // module name runs from the package root to the file
          var name = new String[path.getNameCount() - i];
          for (var j = 0; j < name.length; j++) name[j] = path.getName(i + j).toString();
          var j = name.length - 1;

          // removing the extension from the file name
          name[j] = name[j].substring(0, name[j].length() - 2);

          // we will need the name as a string for later reporting anyway
          var file = path.toString();

          // parse the module
          var module = new Module(name);
          try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            new Parser(file, reader, module.body);
          }
          modules.add(module);
        }
      }
    }
    Etc.dbg(modules);
  }
}
