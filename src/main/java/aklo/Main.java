package aklo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
      Files.walkFileTree(
          p,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
              var file = path.toString();
              if (file.endsWith(".k")) {
                var name = new String[path.getNameCount() - i];
                for (var j = 0; j < name.length; j++) name[j] = path.getName(i + j).toString();
                var j = name.length - 1;
                name[j] = name[j].substring(0, name[j].length() - 2);
                var module = new Module(name);
                modules.add(module);
              }
              return FileVisitResult.CONTINUE;
            }
          });
    }
    Etc.dbg(modules);
  }
}
