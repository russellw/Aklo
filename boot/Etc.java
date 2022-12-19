import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Set;

public class Etc {
  public static List args;
  static int depth;
  static Set<String> tracing;
  public static final boolean windowsp = System.getProperty("os.name").startsWith("Windows");

  public static void init(String[] args1) {
    var r = new Object[args1.length];
    for (var i = 0; i < args1.length; i++) r[i] = encode(args1[i]);
    args = List.of(r);
  }

  public static List compileTimeReadFiles(String dir) {
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
    return List.ofArrayList(r);
  }

  static List listDir(Object dir0) {
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
    return List.ofArrayList(r);
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

  public static void enter(String file, int line, String fname, Object[] args) {
    if (!tracing(fname)) return;
    indent();
    depth++;
    System.out.printf(">%s:%d: %s: ", file, line, fname);
    for (var i = 0; i < args.length; i++) {
      if (i > 0) System.out.print(", ");
      System.out.print(repr(args[i]));
    }
    System.out.println();
  }

  static Object writeStream(Object stream, Object s) {
    var stream1 = (PrintStream) stream;
    for (var c : ((List) s).toArray()) stream1.write((int) c);
    return null;
  }

  static String decode(Object s) {
    return new String(bytes(s), StandardCharsets.UTF_8);
  }

  public static List readFile(Object file) {
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
    var s1 = (List) s;
    var r = new byte[s1.len()];
    for (var i = 0; i < r.length; i++) r[i] = (byte) (int) s1.subscript(i);
    return r;
  }

  static List list(byte[] s) {
    var r = new Object[s.length];
    for (var i = 0; i < r.length; i++) r[i] = (int) s[i] & 0xff;
    return List.of(r);
  }

  public static List encode(String s) {
    return list(s.getBytes(StandardCharsets.UTF_8));
  }

  static double promoteDouble(Object a) {
    if (a instanceof Boolean) return (boolean) a ? 1.0 : 0.0;
    if (a instanceof Integer) return (double) (int) a;
    if (a instanceof Float) return (double) (float) a;
    return (double) a;
  }

  static float promoteFloat(Object a) {
    if (a instanceof Boolean) return (boolean) a ? 1.0f : 0.0f;
    if (a instanceof Integer) return (float) (int) a;
    return (float) a;
  }

  static int promoteInt(Object a) {
    if (a instanceof Boolean) return (boolean) a ? 1 : 0;
    return (int) a;
  }

  static boolean numberp(Object a) {
    return a instanceof Boolean
        || a instanceof Integer
        || a instanceof Float
        || a instanceof Double;
  }

  public static boolean eq(Object a, Object b) {
    if (a == b) return true;
    if (a instanceof List a1) {
      if (!(b instanceof List b1)) return false;
      if (a1.len() != b1.len()) return false;
      for (var i = 0; i < a1.len(); i++) if (!eq(a1.subscript(i), b1.subscript(i))) return false;
      return true;
    }
    if (numberp(a) && numberp(b)) {
      if (a instanceof Double || b instanceof Double) return promoteDouble(a) == promoteDouble(b);
      if (a instanceof Float || b instanceof Float) return promoteFloat(a) == promoteFloat(b);
      return promoteInt(a) == promoteInt(b);
    }
    return a.equals(b);
  }

  static Object eval2(Op2 op, Object a, Object b) {
    if (a instanceof Double || b instanceof Double)
      return op.apply(promoteDouble(a), promoteDouble(b));
    if (a instanceof Float || b instanceof Float) return op.apply(promoteFloat(a), promoteFloat(b));
    if (integerp(a) || integerp(b)) return op.apply(promoteInt(a), promoteInt(b));
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

  static boolean isAscii(List a) {
    if (a.isEmpty()) return false;
    for (var c0 : a.toArray()) {
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
    if (sb.length() >= 1000) throw new OverrunException();
    sb.append(s);
  }

  static void repr(Object a0, StringBuilder sb) {
    if (!(a0 instanceof List a)) {
      appendsb(sb, a0.toString());
      return;
    }
    if (isAscii(a)) {
      appendsb(sb, "\"");
      for (var c0 : a.toArray()) {
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
    for (var i = 0; i < a.len(); i++) {
      if (i > 0) appendsb(sb, ", ");
      repr(a.subscript(i), sb);
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

  public static List slice(Object s0, Object i0, Object j0) {
    // TODO call method directly
    var s = (List) s0;
    var i = (int) i0;
    var j = (int) j0;
    i = Math.max(i, 0);
    j = Math.min(j, s.len());
    if (i > j) return List.of();
    return s.slice(i, j);
  }

  public static List range(Object i0, Object j0) {
    var i = (int) i0;
    var j = j0 instanceof Integer ? (int) j0 : ((List) j0).len();
    var r = new ArrayList<>();
    while (i < j) r.add(i++);
    return List.ofArrayList(r);
  }

  public static int mul(Object a, Object b) {
    return (int) a * (int) b;
  }

  public static List str(Object a) {
    var s = a.toString();
    var r = new ArrayList<>();
    for (var i = 0; i < s.length(); i++) r.add((int) s.charAt(i));
    return List.ofArrayList(r);
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

  public static boolean truth(boolean a) {
    return a;
  }

  public static boolean truth(Object a) {
    if (a instanceof Boolean) return (boolean) a;
    if (a instanceof Integer) return (int) a != 0;
    // TODO other numbers
    if (a instanceof List) return !((List) a).isEmpty();
    return true;
  }

  public static boolean integerp(Object a) {
    return a instanceof Boolean || a instanceof Integer;
  }

  public static Object get(Object record, Object key) {
    // TODO this should be a method
    for (var entry : ((List) record).toArray()) {
      var entry1 = (List) entry;
      if (entry1.subscript(0).equals(key)) return entry1.subscript(1);
    }
    return 0;
  }
}
