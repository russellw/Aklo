package aklo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class Etc {
  public static final Boolean isWindows = System.getProperty("os.name").startsWith("Windows");

  public static List<Object> listVal(Object a) {
    if (a instanceof List a1) return a1;
    return List.of(a);
  }

  public static int intVal(Object a) {
    // TODO names
    return switch (a) {
      case BigInteger a1 -> a1.intValueExact();
      case Boolean a1 -> a1 ? 1 : 0;
      default -> throw new IllegalArgumentException(a.toString());
    };
  }

  @SuppressWarnings("unused")
  public static void exit(Object a) {
    System.exit(intVal(a));
  }

  public static void print(Object a) {
    switch (a) {
      case BigInteger a0 -> {
        var a1 = a0.intValueExact();
        if (!(0 <= a1 && a1 <= 255)) throw new IllegalArgumentException(a.toString());
        System.out.write(a1);
      }
      case List a1 -> {
        for (var b : a1) print(b);
      }
      default -> System.out.writeBytes(a.toString().getBytes(StandardCharsets.UTF_8));
    }
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
    return switch (a) {
      case Boolean a1 -> a1;
      case BigInteger a1 -> a1.signum() != 0;
      case List a1 -> !a1.isEmpty();
      case Float a1 -> a1 != 0.0f;
      case Double a1 -> a1 != 0.0;
      case BigRational a1 -> a1.signum() != 0;
      default -> true;
    };
  }

  @SuppressWarnings("unused")
  public static void dbg(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }

  public static Object mul(Object a, Object b) {
    return Binary.eval(new Mul(null, null), a, b);
  }

  public static Object add(Object a, Object b) {
    return Binary.eval(new Add(null, null), a, b);
  }

  public static Object bitAnd(Object a, Object b) {
    return Binary.eval(new And(null, null), a, b);
  }

  public static Object bitNot(Object a) {
    return Unary.eval(new Not(null), a);
  }

  public static Object bitOr(Object a, Object b) {
    return Binary.eval(new Or(null, null), a, b);
  }

  public static Object bitXor(Object a, Object b) {
    return Binary.eval(new Xor(null, null), a, b);
  }

  public static Object cmp(Object a, Object b) {
    return Binary.eval(new Cmp(null, null), a, b);
  }

  public static Object div(Object a, Object b) {
    return Binary.eval(new Div(null, null), a, b);
  }

  public static Object divInt(Object a, Object b) {
    return Binary.eval(new DivInt(null, null), a, b);
  }

  public static Object eqNum(Object a, Object b) {
    return Binary.eval(new EqNum(null, null), a, b);
  }

  public static Object exp(Object a, Object b) {
    return Binary.eval(new Exp(null, null), a, b);
  }

  public static Object le(Object a, Object b) {
    return Binary.eval(new Le(null, null), a, b);
  }

  public static Object len(Object s) {
    return BigInteger.valueOf(((List) s).size());
  }

  static BigInteger integerVal(Object a) {
    if (a instanceof BigInteger a1) return a1;
    throw new IllegalArgumentException(a.toString());
  }

  public static List<Object> range(Object i, Object j) {
    var i1 = integerVal(i);
    var j1 = integerVal(j);
    var r = new ArrayList<>();
    while (i1.compareTo(j1) < 0) {
      r.add(i1);
      i1 = i1.add(BigInteger.ONE);
    }
    return r;
  }

  public static Object lt(Object a, Object b) {
    return Binary.eval(new Lt(null, null), a, b);
  }

  public static Object neg(Object a) {
    return Unary.eval(new Neg(null), a);
  }

  public static Object rem(Object a, Object b) {
    return Binary.eval(new Rem(null, null), a, b);
  }

  public static Object shl(Object a, Object b) {
    return Binary.eval(new Shl(null, null), a, b);
  }

  public static Object sub(Object a, Object b) {
    return Binary.eval(new Sub(null, null), a, b);
  }

  public static Object subscript(Object s, Object i) {
    return ((List) s).get(intVal(i));
  }

  public static List<Object> slice(Object s, Object i, Object j) {
    return ((List) s).subList(intVal(i), intVal(j));
  }

  static String typeof(Object a) {
    return switch (a) {
      case Var a1 -> a1.type;
      case Instruction a1 -> a1.type();
      default -> a.getClass().descriptorString();
    };
  }

  public static Object shr(Object a, Object b) {
    return Binary.eval(new Shr(null, null), a, b);
  }
}
