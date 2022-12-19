package aklo;

import java.io.IOException;
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
    var packages = new ArrayList<String>();
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
      packages.add(s);
    }

    // parse
    System.out.println(packages);
  }
}
