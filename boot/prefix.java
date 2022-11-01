import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

class Main {
  public static void main(String[] args) {
    for (var s : args) Etc.argv.add(Etc.encode(s));
    program.run();
  }
}

@SuppressWarnings("unchecked")
class Etc {
  static List<Object> argv = new ArrayList<>();

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

  static int and(Object a, Object b) {
    return (int) a & (int) b;
  }

  static int or(Object a, Object b) {
    return (int) a | (int) b;
  }

  static int xor(Object a, Object b) {
    return (int) a ^ (int) b;
  }

  static int shl(Object a, Object b) {
    return (int) a << (int) b;
  }

  static int shr(Object a, Object b) {
    return (int) a >> (int) b;
  }

  static int ushr(Object a, Object b) {
    return (int) a >>> (int) b;
  }

  @SuppressWarnings("unused")
  static void show(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }

  static List<Object> slice(Object s, Object i, Object j) {
    return ((List<Object>) s).subList((int) i, (int) j);
  }

  static List<Object> range(Object i, Object j) {
    var i1 = (int) i;
    // TODO: do the right thing when the argument is a list
    var j1 = (int) j;
    var r = new ArrayList<Object>();
    while (i1 < j1) r.add(i1++);
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
    var r = new ArrayList<Object>((List<Object>) s);
    r.addAll((List<Object>) t);
    return r;
  }

  static int len(Object s) {
    return ((List<Object>) s).size();
  }

  static Object exit(Object a) {
    System.exit((int) a);
    return null;
  }

  static int neg(Object a) {
    return -(int) a;
  }

  static int not(Object a) {
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
    var s1 = (List<Object>) s;
    var i1 = (int) i;
    if (!(0 <= i1 && i1 < s1.size())) return 0;
    return s1.get(i1);
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

  static boolean isnum(Object a) {
    return a instanceof Integer;
  }

  static boolean issym(Object a) {
    return a instanceof Sym;
  }

  static Object get(Object record, Object key) {
    for (var entry : (List<Object>) record) {
      var entry1 = (List<Object>) entry;
      if (entry1.get(0).equals(key)) return entry1.get(1);
    }
    return 0;
  }

  static boolean islist(Object a) {
    return a instanceof List;
  }
}

@SuppressWarnings("unchecked")
class Sym {
  static final Map<String, Sym> syms = new HashMap<>();
  static final Map<String, Integer> suffixes = new HashMap<>();
  final String stem;
  String name;

  Sym() {
    stem = null;
  }

  Sym(String stem) {
    this.stem = stem;
  }

  static Sym intern(String name) {
    var a = syms.get(name);
    if (a == null) {
      a = new Sym();
      a.name = name;
      syms.put(name, a);
    }
    return a;
  }

  static Sym intern(Object name) {
    var sb = new StringBuilder();
    for (var c : (List<Object>) name) sb.append((char) (int) c);
    return intern(sb.toString());
  }

  @Override
  public String toString() {
    if (name == null) {
      var i = suffixes.get(stem);
      if (i == null) {
        name = stem;
        i = 1;
      } else name = stem + i++;
      suffixes.put(stem, i);
      name = '#' + name;
    }
    return name;
  }
}
