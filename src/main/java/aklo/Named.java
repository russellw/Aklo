package aklo;

import java.util.Collection;
import java.util.HashSet;

abstract class Named {
  String name;

  Named(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  static void unique(Collection<? extends Named> s) {
    var names = new HashSet<String>();
    for (var a : s) {
      if (names.add(a.name)) continue;
      for (var i = 1; ; i++) {
        var t = a.name + i;
        if (names.add(t)) {
          a.name = t;
          break;
        }
      }
    }
  }
}
