import java.io.*;
import java.util.*;

class Main {
  public static void main(String[] args) {
    new Main1().run();
  }
}

class Etc {
  static int add(Object a, Object b) {
    return (int) a + (int) b;
  }

  static List<Object> range(Object i, Object j) {
    var i1 = (int) i;
    var j1 = (int) j;
    var r = new ArrayList<Object>();
    while (i1 < j1) r.add(i1++);
    return r;
  }

  static int mul(Object a, Object b) {
    return (int) a * (int) b;
  }

  static List<Object> append(Object a, Object b) {
    var a1 = (List<Object>) a;
    var r = new ArrayList<Object>(a1);
    r.add(b);
    return r;
  }

  static List<Object> cat(Object a, Object b) {
    var a1 = (List<Object>) a;
    var b1 = (List<Object>) b;
    var r = new ArrayList<Object>(a1);
    r.addAll(b1);
    return r;
  }

  static void eprint(Object a) {
    fprint(System.err, a);
  }

  static void fprint(PrintStream stream, Object a) {
    if (a instanceof List) {
      var a1 = (List<Object>) a;
      for (var c : a1) stream.print((char) (int) c);
      return;
    }
    throw new IllegalArgumentException(a.toString());
  }

  static int len(Object a) {
    var a1 = (List<Object>) a;
    return a1.size();
  }

  static int neg(Object a) {
    return -(int) a;
  }

  static void print(Object a) {
    fprint(System.out, a);
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

  static boolean eq(Object a, Object b) {
    return a.equals(b);
  }

  static Object subscript(Object a, Object i) {
    var a1 = (List<Object>) a;
    var i1 = (int) i;
    return a1.get(i1);
  }

  static boolean truth(boolean a) {
    return a;
  }

  static boolean truth(Object a) {
    if (a instanceof Integer) return (int) a != 0;
    return true;
  }
}
