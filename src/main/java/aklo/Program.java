package aklo;

import java.util.HashSet;
import java.util.List;

public final class Program {
  public Program(List<Module> modules) {
    for (var module : modules) {
      // generate explicit variable declarations
      module.walkFns(
          f -> {
            for (var a : f.body)
              a.walk(
                  b -> {
                    switch (b.tag()) {
                      case VAR -> f.vars.add((Var) b);
                    }
                  });
          });

      // check for inconsistent declarations
      module.walkFns(
          f -> {
            var params = new HashSet<String>();
            for (var x : f.params)
              if (!params.add(x.name))
                throw new CompileError(x.loc, x.name + ": duplicate parameter name");
          });
    }
  }
}
