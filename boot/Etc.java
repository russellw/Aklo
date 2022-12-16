import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Etc {
  public static List<Object> args = new ArrayList<>();
  static int depth;
  static Set<String> tracing;
  public static final boolean windowsp = System.getProperty("os.name").startsWith("Windows");

  public static List<Object> compileTimeReadFiles(String dir) {
    // in the full language, compileTimeReadFiles works at compile time
    // thereby generating arbitrarily large compile-time constants
    // because they contain the full text of the files read
    // but Java does not have a good way to embed arbitrarily large arrays in source code
    // due to the limit on method size
    // so during boot, the 'compiled to Java' version of the main compiler
    // just performs compileTimeReadFiles at runtime
    // the tradeoff is that the boot version of the compiler
    // cannot be redistributed separately from its source code
    // this is acceptable for a program that will only be used once
    var r = new ArrayList<>();
    try {
      Files.walkFileTree(
          Paths.get(dir),
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
              var file = path.toString();
              if (file.endsWith(".k")) {
                var file1 = encode(file);
                r.add(List.of(file1, readFile(file1)));
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return r;
  }

  static List<Object> listDir(Object dir0) {
    var dir = decode(dir0);
    var r = new ArrayList<>();
    try {
      Files.walkFileTree(
          Paths.get(dir),
          Set.of(),
          1,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs0) {
              var file = path.toString().substring(dir.length() + 1);
              var attrs = List.of();
              if (attrs0.isDirectory()) attrs = List.of(Sym.intern("dir?"), 1);
              r.add(List.of(encode(file), attrs));
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return r;
  }

  static boolean dirp(Object file) {
    return Files.isDirectory(Path.of(decode(file)));
  }

  static void indent() {
    for (var i = 0; i < depth; i++) System.out.print(' ');
  }

  public static void leave(String file, int line, String fname, Object r) {
    if (!tracing(fname)) return;
    depth--;
    indent();
    System.out.printf("<%s:%d: %s: %s\n", file, line, fname, repr(r));
  }

  static boolean tracing(String fname) {
    if (tracing == null) return false;
    if (tracing.isEmpty()) return true;
    return tracing.contains(fname);
  }

  public static void enter(String file, int line, String fname, List<Object> args) {
    if (!tracing(fname)) return;
    indent();
    depth++;
    System.out.printf(">%s:%d: %s: ", file, line, fname);
    for (var i = 0; i < args.size(); i++) {
      if (i > 0) System.out.print(", ");
      System.out.print(repr(args.get(i)));
    }
    System.out.println();
  }

  static Object writeStream(Object stream, Object s) {
    var stream1 = (PrintStream) stream;
    for (var c : (List<Object>) s) stream1.write((int) c);
    return null;
  }

  static String decode(Object s) {
    return new String(bytes(s), StandardCharsets.UTF_8);
  }

  public static List<Object> readFile(Object file) {
    try {
      return list(Files.readAllBytes(Path.of(decode(file))));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Double parseDouble(Object s) {
    return Double.parseDouble(decode(s));
  }

  public static Float parseFloat(Object s) {
    return Float.parseFloat(decode(s));
  }

  public static void writeFile(Object file, Object s) {
    try {
      Files.write(Path.of(decode(file)), bytes(s));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static byte[] bytes(Object s) {
    var s1 = (List<Object>) s;
    var r = new byte[s1.size()];
    for (var i = 0; i < r.length; i++) r[i] = (byte) (int) s1.get(i);
    return r;
  }

  static List<Object> list(byte[] s) {
    var r = new Object[s.length];
    for (var i = 0; i < r.length; i++) r[i] = (int) s[i] & 0xff;
    return List.of(r);
  }

  public static List<Object> encode(String s) {
    return list(s.getBytes(StandardCharsets.UTF_8));
  }

  static Object eval2(Op2 op, Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return op.apply((double) a, (double) b);
    if (a instanceof Float || b instanceof Float) return op.apply((float) a, (float) b);
    if (a instanceof Integer || b instanceof Integer) return op.apply((int) a, (int) b);
    throw new IllegalArgumentException(a.toString() + ',' + b.toString());
  }

  public static int add(int a, int b) {
    return a + b;
  }

  public static Object add(Object a, Object b) {
    return eval2(
        new Op2() {
          @Override
          Object apply(double a, double b) {
            return a + b;
          }

          @Override
          Object apply(float a, float b) {
            return a + b;
          }

          @Override
          Object apply(int a, int b) {
            return a + b;
          }
        },
        a,
        b);
  }

  public static int rem(Object a, Object b) {
    return (int) a % (int) b;
  }

  public static int bitAnd(Object a, Object b) {
    return (int) a & (int) b;
  }

  public static int bitOr(Object a, Object b) {
    return (int) a | (int) b;
  }

  public static int bitXor(Object a, Object b) {
    return (int) a ^ (int) b;
  }

  public static int shl(Object a, Object b) {
    return (int) a << (int) b;
  }

  public static int shr(Object a, Object b) {
    return (int) a >> (int) b;
  }

  static boolean isAscii(List<Object> a) {
    if (a.isEmpty()) return false;
    for (var c0 : a) {
      if (!(c0 instanceof Integer)) return false;
      var c = (int) c0;
      switch (c) {
        case '\t':
        case '\r':
        case '\n':
          continue;
      }
      if (!(' ' <= c && c <= 126)) return false;
    }
    return true;
  }

  static void appendsb(StringBuilder sb, String s) {
    if (sb.length() >= 4096) throw new OverrunException();
    sb.append(s);
  }

  static void repr(Object a0, StringBuilder sb) {
    if (!(a0 instanceof List)) {
      appendsb(sb, a0.toString());
      return;
    }
    var a = (List<Object>) a0;
    if (isAscii(a)) {
      appendsb(sb, "\"");
      for (var c0 : a) {
        var c = (int) c0;
        switch (c) {
          case '\t' -> appendsb(sb, "\\t");
          case '\r' -> appendsb(sb, "\\r");
          case '\n' -> appendsb(sb, "\\n");
          default -> appendsb(sb, Character.toString(c));
        }
      }
      appendsb(sb, "\"");
      return;
    }
    appendsb(sb, "[");
    for (var i = 0; i < a.size(); i++) {
      if (i > 0) appendsb(sb, ", ");
      repr(a.get(i), sb);
    }
    appendsb(sb, "]");
  }

  static String repr(Object a) {
    var sb = new StringBuilder();
    try {
      repr(a, sb);
    } catch (OverrunException e) {
      sb.append("...");
    }
    return sb.toString();
  }

  static void dbg(String file, int line, String fname, String name, Object val) {
    System.out.printf("%s:%d: %s: %s: %s\n", file, line, fname, name, repr(val));
  }

  static void dbg(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }

  public static List<Object> slice(Object s0, Object i0, Object j0) {
    var s = (List<Object>) s0;
    var i = (int) i0;
    var j = (int) j0;
    i = Math.max(i, 0);
    j = Math.min(j, s.size());
    if (i > j) return List.of();
    return s.subList(i, j);
  }

  public static List<Object> range(Object i0, Object j0) {
    var i = (int) i0;
    var j = j0 instanceof Integer ? (int) j0 : len(j0);
    var r = new ArrayList<>();
    while (i < j) r.add(i++);
    return r;
  }

  public static int mul(Object a, Object b) {
    return (int) a * (int) b;
  }

  public static List<Object> str(Object a) {
    var s = a.toString();
    var r = new ArrayList<>();
    for (var i = 0; i < s.length(); i++) r.add((int) s.charAt(i));
    return r;
  }

  public static List<Object> cons(Object... s) {
    var r = new ArrayList<>();
    r.addAll(Arrays.asList(s).subList(0, s.length - 1));
    r.addAll((List<Object>) s[s.length - 1]);
    return r;
  }

  public static List<Object> cat(Object s, Object t) {
    var r = new ArrayList<>((List<Object>) s);
    r.addAll((List<Object>) t);
    return r;
  }

  static List<Object> prepend(Object a, Object s) {
    var r = new ArrayList<>();
    r.add(a);
    r.addAll((List<Object>) s);
    return r;
  }

  public static List<Object> append(Object s, Object a) {
    var r = new ArrayList<>((List<Object>) s);
    r.add(a);
    return r;
  }

  public static int len(Object s) {
    return ((List<Object>) s).size();
  }

  static Object exit(Object a) {
    throw new RuntimeException(a.toString());
  }

  public static int neg(Object a) {
    return -(int) a;
  }

  public static int bitNot(Object a) {
    return ~(int) a;
  }

  public static Object sub(Object a, Object b) {
    return eval2(
        new Op2() {
          @Override
          Object apply(double a, double b) {
            return a - b;
          }

          @Override
          Object apply(float a, float b) {
            return a - b;
          }

          @Override
          Object apply(int a, int b) {
            return a - b;
          }
        },
        a,
        b);
  }

  public static int sub(int a, int b) {
    return (int) a - (int) b;
  }

  public static int div(Object a, Object b) {
    return (int) a / (int) b;
  }

  public static boolean lt(Object a, Object b) {
    return (int) a < (int) b;
  }

  public static boolean le(Object a, Object b) {
    return (int) a <= (int) b;
  }

  public static Object subscript(Object s, Object i) {
    return ((List<Object>) s).get((int) i);
  }

  public static boolean truth(boolean a) {
    return a;
  }

  public static boolean truth(Object a) {
    if (a instanceof Integer) return (int) a != 0;
    if (a instanceof List) return ((List<Object>) a).size() != 0;
    if (a instanceof Boolean) return (boolean) a;
    return true;
  }

  public static boolean integerp(Object a) {
    return a instanceof Integer;
  }

  public static Object get(Object record, Object key) {
    for (var entry : (List<Object>) record) {
      var entry1 = (List<Object>) entry;
      if (entry1.get(0).equals(key)) return entry1.get(1);
    }
    return 0;
  }
}
