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

  public static BigInteger parseInt(Object s) {
    return new BigInteger(decode(s));
  }

  public static BigRational parseRat(Object s) {
    return BigRational.of(decode(s));
  }

  public static Float parseFloat(Object s) {
    return Float.parseFloat(decode(s));
  }

  public static Sym intern(Object name) {
    return Sym.intern(decode(name));
  }

  public static Double parseDouble(Object s) {
    return Double.parseDouble(decode(s));
  }

  public static BigInteger parseInt(Object s, Object base) {
    return new BigInteger(decode(s), intVal(base));
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
    // TODO should this accept atoms?
    var r = new ArrayList<>(listVal(a));
    r.addAll(listVal(b));
    return r;
  }

  public static boolean truth(Object a) {
    if (a instanceof Boolean a1) return a1;
    if (a instanceof BigInteger a1) return a1.signum() != 0;
    if (a instanceof List a1) return !a1.isEmpty();
    if (a instanceof Float a1) return a1 != 0.0f;
    if (a instanceof Double a1) return a1 != 0.0;
    if (a instanceof BigRational a1) return a1.signum() != 0;
    return true;
  }

  public static void dbg(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }
}
