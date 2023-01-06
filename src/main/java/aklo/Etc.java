package aklo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class Etc {
  private Etc() {}

  public static List<Object> listVal(Object a) {
    if (a instanceof List a1) return a1;
    return List.of(a);
  }

  public static int intVal(Object a) {
    if (a instanceof BigInteger a1) return a1.intValueExact();
    if (a instanceof Boolean a1) return a1 ? 1 : 0;
    throw new IllegalArgumentException(a.toString());
  }

  public static void exit(Object a) {
    System.exit(intVal(a));
  }

  public static BigInteger toInteger(Object a) {
    if (a instanceof BigInteger a1) return a1;
    return (boolean) a ? BigInteger.ONE : BigInteger.ZERO;
  }

  public static boolean isInteger(Object a) {
    return a instanceof BigInteger || a instanceof Boolean;
  }

  public static void print(Object a) {
    if (a instanceof BigInteger a0) {
      var a1 = a0.intValueExact();
      if (!(0 <= a1 && a1 <= 255)) throw new IllegalArgumentException(a.toString());
      System.out.write(a1);
      return;
    }
    if (a instanceof List a1) {
      for (var b : a1) print(b);
      return;
    }
    System.out.writeBytes(a.toString().getBytes(StandardCharsets.UTF_8));
  }

  public static String decode(Object s) {
    return new String(bytes(s), StandardCharsets.UTF_8);
  }

  public static byte[] bytes(Object s) {
    var s1 = (List) s;
    var r = new byte[s1.size()];
    for (var i = 0; i < r.length; i++) r[i] = (byte) ((BigInteger) s1.get(i)).intValue();
    return r;
  }

  public static List<Object> list(byte[] s) {
    var r = new Object[s.length];
    for (var i = 0; i < r.length; i++) r[i] = BigInteger.valueOf(s[i] & 0xff);
    return List.of(r);
  }

  public static List<Object> encode(String s) {
    return list(s.getBytes(StandardCharsets.UTF_8));
  }

  public static List<Object> str(Object a) {
    return encode(a.toString());
  }

  public static List<Object> cat(Object a, Object b) {
    var r = new ArrayList<>(listVal(a));
    r.addAll(listVal(b));
    return r;
  }

  public static boolean truth(Object a) {
    if (a instanceof Boolean a1) return a1;
    if (a instanceof BigInteger a1) return a1.signum() != 0;
    // TODO other numbers
    if (a instanceof List a1) return !a1.isEmpty();
    return true;
  }

  public static void dbg(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }
}
