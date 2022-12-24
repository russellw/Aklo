package aklo;

import java.util.*;

public final class Program {
  private static final class Context {
    // unlabeled loops
    final Block continueTarget;
    final Block breakTarget;

    // labeled loops
    final Map<String, Block> continueLabels = new HashMap<>();
    final Map<String, Block> breakLabels = new HashMap<>();

    Context(Block continueTarget, Block breakTarget) {
      this.continueTarget = continueTarget;
      this.breakTarget = breakTarget;
    }
  }

  public final List<Var> vars = new ArrayList<>();
  public final List<Fn> fns = new ArrayList<>();

  // convert to basic blocks
  private Block block;

  private void add(Term a) {
    block.insns.add(a);
  }

  private Term term(Context context, Term a) {
    switch (a.tag()) {
      case RETURN -> {
        add(a);
      }
    }
    return a;
  }

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
