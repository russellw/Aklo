import java.util.HashMap;
import java.util.Map;

public class Sym {
  static final Map<String, Sym> syms = new HashMap<>();
  static final Map<String, Integer> suffixes = new HashMap<>();
  final String stem;
  String name;

  // interned symbols
  // TODO Does access need to be specified?
  private Sym() {
    stem = null;
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

  public static Sym intern(Object name) {
    return intern(Etc.decode(name));
  }

  // uninterned symbols
  public Sym(Object stem) {
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
