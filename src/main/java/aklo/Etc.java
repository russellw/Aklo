package aklo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class Etc {
  private Etc() {}

  public static String unesc(String s) {
    var sb = new StringBuilder();
    for (var i = 0; i < s.length(); ) {
      var c = s.charAt(i++);
      if (c == '\\' && i < s.length()) {
        c = s.charAt(i++);
        switch (c) {
          case 'b' -> c = '\b';
          case 'f' -> c = '\f';
          case 'n' -> c = '\n';
          case 'r' -> c = '\r';
          case 't' -> c = '\t';
          case 'a' -> c = 7;
          case 'e' -> c = 0x1b;
          case 'v' -> c = 0xb;
          case '0', '1', '2', '3', '4', '5', '6', '7' -> {
            i--;
            var n = 0;
            for (int j = 0; j < 3 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 8);
              if (d < 0) break;
              i++;
              n = n * 8 + d;
            }
            c = (char) n;
          }
          case 'u' -> {
            var n = 0;
            for (int j = 0; j < 4 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 16);
              if (d < 0) break;
              i++;
              n = n * 16 + d;
            }
            c = (char) n;
          }
          case 'x' -> {
            var n = 0;
            for (int j = 0; j < 2 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 16);
              if (d < 0) break;
              i++;
              n = n * 16 + d;
            }
            c = (char) n;
          }
          case 'U' -> {
            var n = 0;
            for (int j = 0; j < 8 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 16);
              if (d < 0) break;
              i++;
              n = n * 16 + d;
            }
            sb.appendCodePoint(n);
            continue;
          }
        }
      }
      sb.append(c);
    }
    return sb.toString();
  }

  public static List<Object> listVal(Object a) {
    if (a instanceof List) return (List<Object>) a;
    return List.of(a);
  }

  public static int intVal(Object a) {
    if (a instanceof BigInteger a1) return a1.intValueExact();
    throw new IllegalArgumentException(a.toString());
  }

  public static void print(Object a) {
    for (var b : (List) a) System.out.write(intVal(b));
  }

  public static List<Object> cat(Object a, Object b) {
    var r = new ArrayList<>(listVal(a));
    r.addAll(listVal(b));
    return r;
  }

  public static boolean truth(Object a) {
    if (a instanceof Boolean) return (boolean) a;
    if (a instanceof Integer) return (int) a != 0;
    // TODO other numbers
    if (a instanceof List) return !((List) a).isEmpty();
    return true;
  }

  public static void dbg(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }
}
