import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

class Main {
  public static void main(String[] args) {
    for (var s : args) Etc.args.add(Etc.encode(s));
    program.run();
  }
}

@SuppressWarnings("unchecked")
class Etc {
  static class OverrunException extends RuntimeException {}

  static List<Object> args = new ArrayList<>();
  static int depth;
  static Set<String> tracing;

  static void indent() {
    for (var i = 0; i < depth; i++) System.out.print(' ');
  }

  static void leave(String file, int line, String fname, Object r) {
    if (!tracing(fname)) return;
    depth--;
    indent();
    System.out.printf("<%s:%d: %s: %s\n", file, line, fname, repr(r));
  }

  private static boolean tracing(String fname) {
    if (tracing == null) return false;
    if (tracing.isEmpty()) return true;
    return tracing.contains(fname);
  }

  static void enter(String file, int line, String fname, List<Object> args) {
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

  static Object writestream(Object stream, Object s) {
    var stream1 = (PrintStream) stream;
    for (var c : (List<Object>) s) stream1.write((int) c);
    return null;
  }

  static String decode(Object s) {
    return new String(bytes(s), StandardCharsets.UTF_8);
  }

  static List<Object> readfile(Object file) {
    try {
      return list(Files.readAllBytes(Path.of(decode(file))));
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

  static List<Object> encode(String s) {
    return list(s.getBytes(StandardCharsets.UTF_8));
  }

  static int add(Object a, Object b) {
    return (int) a + (int) b;
  }

  static int rem(Object a, Object b) {
    return (int) a % (int) b;
  }

  static int bitand(Object a, Object b) {
    return (int) a & (int) b;
  }

  static int bitor(Object a, Object b) {
    return (int) a | (int) b;
  }

  static int bitxor(Object a, Object b) {
    return (int) a ^ (int) b;
  }

  static int shl(Object a, Object b) {
    return (int) a << (int) b;
  }

  static int shr(Object a, Object b) {
    return (int) a >> (int) b;
  }

  static boolean isascii(List<Object> a) {
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

  static void append(StringBuilder sb, String s) {
    if (sb.length() >= 4096) throw new OverrunException();
    sb.append(s);
  }

  static void repr(Object a0, StringBuilder sb) {
    if (!(a0 instanceof List)) {
      append(sb, a0.toString());
      return;
    }
    var a = (List<Object>) a0;
    if (isascii(a)) {
      append(sb, "\"");
      for (var c0 : a) {
        var c = (int) c0;
        switch (c) {
          case '\t' -> append(sb, "\\t");
          case '\r' -> append(sb, "\\r");
          case '\n' -> append(sb, "\\n");
          default -> append(sb, Character.toString(c));
        }
      }
      append(sb, "\"");
      return;
    }
    append(sb, "(");
    for (var i = 0; i < a.size(); i++) {
      if (i > 0) append(sb, " ");
      repr(a.get(i), sb);
    }
    append(sb, ")");
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

  static void show(String file, int line, String fname, String name, Object val) {
    System.out.printf("%s:%d: %s: %s: %s\n", file, line, fname, name, repr(val));
  }

  static void show(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }

  static List<Object> slice(Object s0, Object i0, Object j0) {
    var s = (List<Object>) s0;
    var i = (int) i0;
    var j = (int) j0;
    i = Math.max(i, 0);
    j = Math.min(j, s.size());
    if (i > j) return List.of();
    return s.subList(i, j);
  }

  static List<Object> range(Object i0, Object j0) {
    var i = (int) i0;
    var j = j0 instanceof Integer ? (int) j0 : len(j0);
    var r = new ArrayList<>();
    while (i < j) r.add(i++);
    return r;
  }

  static int mul(Object a, Object b) {
    return (int) a * (int) b;
  }

  static List<Object> str(Object a) {
    var s = a.toString();
    var r = new ArrayList<>();
    for (var i = 0; i < s.length(); i++) r.add((int) s.charAt(i));
    return r;
  }

  static List<Object> cons(Object... s) {
    var r = new ArrayList<>();
    r.addAll(Arrays.asList(s).subList(0, s.length - 1));
    r.addAll((List<Object>) s[s.length - 1]);
    return r;
  }

  static List<Object> cat(Object s, Object t) {
    var r = new ArrayList<>((List<Object>) s);
    r.addAll((List<Object>) t);
    return r;
  }

  static List<Object> cat1(Object s, Object a) {
    var r = new ArrayList<>((List<Object>) s);
    r.add(a);
    return r;
  }

  static int len(Object s) {
    return ((List<Object>) s).size();
  }

  static Object exit(Object a) {
    throw new RuntimeException(a.toString());
  }

  static int neg(Object a) {
    return -(int) a;
  }

  static int bitnot(Object a) {
    return ~(int) a;
  }

  static int sub(Object a, Object b) {
    return (int) a - (int) b;
  }

  static boolean lt(Object a, Object b) {
    return (int) a < (int) b;
  }

  static boolean le(Object a, Object b) {
    return (int) a <= (int) b;
  }

  static Object subscript(Object s, Object i) {
    return ((List<Object>) s).get((int) i);
  }

  static boolean truth(boolean a) {
    return a;
  }

  static boolean truth(Object a) {
    if (a instanceof Integer) return (int) a != 0;
    if (a instanceof List) return ((List<Object>) a).size() != 0;
    if (a instanceof Boolean) return (boolean) a;
    return true;
  }

  static boolean intp(Object a) {
    return a instanceof Integer;
  }

  static boolean symp(Object a) {
    return a instanceof Sym;
  }

  static Object get(Object record, Object key) {
    for (var entry : (List<Object>) record) {
      var entry1 = (List<Object>) entry;
      if (entry1.get(0).equals(key)) return entry1.get(1);
    }
    return 0;
  }

  static boolean listp(Object a) {
    return a instanceof List;
  }
}

class Sym {
  static final Map<String, Sym> syms = new HashMap<>();
  static final Map<String, Integer> suffixes = new HashMap<>();
  final String stem;
  String name;

  // interned symbols
  private Sym() {
    stem = null;
  }

  private static Sym intern(String name) {
    var a = syms.get(name);
    if (a == null) {
      a = new Sym();
      a.name = name;
      syms.put(name, a);
    }
    return a;
  }

  static Sym intern(Object name) {
    return intern(Etc.decode(name));
  }

  // uninterned symbols
  Sym(Object stem) {
    this.stem = Etc.decode(stem);
  }

  @Override
  public String toString() {
    if (name == null) {
      int i = suffixes.getOrDefault(stem, 0);
      name = '_' + stem + i++;
      suffixes.put(stem, i);
    }
    return name;
  }
}
