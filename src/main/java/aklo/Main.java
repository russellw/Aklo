package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import org.objectweb.asm.*;

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
    ClassWriter cw = new ClassWriter(0);
    cw.visit(
        V1_5,
        ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
        "pkg/Comparable",
        null,
        "java/lang/Object",
        new String[] {"pkg/Mesurable"});
    cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I", null, new Integer(-1))
        .visitEnd();
    cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I", null, new Integer(0))
        .visitEnd();
    cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I", null, new Integer(1))
        .visitEnd();
    cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "compareTo", "(Ljava/lang/Object;)I", null, null)
        .visitEnd();
    cw.visitEnd();
    byte[] b = cw.toByteArray();
    Files.write(Path.of("a.class"), b);

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
          var file = path.toString();
          var loc = new Loc(file, 1);

          // module name runs from the package root to the file
          var names = new String[path.getNameCount() - i];
          for (var j = 0; j < names.length; j++) names[j] = path.getName(i + j).toString();
          var j = names.length - 1;
          names[j] = names[j].substring(0, names[j].length() - 2);

          // parse the module
          var module = new Module(loc, names);
          try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            new Parser(file, reader, module);
          }
          modules.add(module);
        }
      }
    }

    // convert to basic blocks
    new Program(modules);
  }
}
