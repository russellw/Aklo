package aklo;

import java.util.HashMap;
import java.util.Map;

public final class Sym {
  private static final Map<String, Sym> syms = new HashMap<>();
  private final String name;

  private Sym(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static Sym intern(String name) {
    var a = syms.get(name);
    if (a == null) {
      a = new Sym(name);
      syms.put(name, a);
    }
    return a;
  }
}
