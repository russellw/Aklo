package aklo;

import java.util.HashSet;
import java.util.List;

public final class SSA {
  private SSA() {}

  public static void convert(List<Module> modules) {
    // check for inconsistent declarations
    for (var module : modules) {
      module.walkFns(
          a -> {
            var params = new HashSet<String>();
            for (var x : a.params)
              if (!params.add(x.name))
                throw new CompileError(x.loc, x.name + ": duplicate parameter name");
          });
    }
  }
}
