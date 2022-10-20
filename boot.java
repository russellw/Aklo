import java.io.*;
import java.util.*;

class Main {
  public static void main(String[] args) {
    new Main1.run();
  }
}

class Etc {
  int add(Object a, Object b) {
    return (int) a + (int) b;
  }

  List<Object> append(Object a, Object b) {
    var a1 = (List<Object>) a;
    var r = new ArrayList<Object>(a1);
    r.add(b);
    return r;
  }

  List<Object> cat(Object a, Object b) {
    var a1 = (List<Object>) a;
    var b1 = (List<Object>) b;
    var r = new ArrayList<Object>(a1);
    r.addAll(b1);
    return r;
  }

  void eprint(Object a) {
    fprint(System.err, a);
  }

  void fprint(PrintStream stream, Object a) {
    if (a instanceof List) {
      var a1 = (List<Object>) a;
      for (var c : a1) stream.print((char) (int) c);
      return;
    }
    throw new IllegalArgumentException(a.toString());
  }

  int len(Object a) {
    var a1 = (List<Object>) a;
    return a1.size();
  }

  int neg(Object a) {
    return -(int) a;
  }

  void print(Object a) {
    fprint(System.out, a);
  }

  int sub(Object a, Object b) {
    return (int) a - (int) b;
  }

  Object subscript(Object a, Object i) {
    var a1 = (List<Object>) a;
    var i1 = (int) i;
    return a1.get(i1);
  }

  boolean truth(Object a) {
    if (a instanceof Integer) return (int) a != 0;
    return true;
  }
}
