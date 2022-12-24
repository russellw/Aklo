package aklo;

import java.util.*;

public final class Program {
  private static final class Context {
    final Context outer;
    final String label;
    final Block continueTarget;
    final Block breakTarget;

    Context(Context outer, String label, Block continueTarget, Block breakTarget) {
      this.outer = outer;
      this.label = label;
      this.continueTarget = continueTarget;
      this.breakTarget = breakTarget;
    }
  }

  public final List<Var> vars = new ArrayList<>();
  public final List<Fn> fns = new ArrayList<>();

  // convert to basic blocks
  private Block block;
  private Fn fn;

  private void add(Term a) {
    block.insns.add(a);
  }

  private Term term(Context context, Term a) {
    switch (a.tag()) {
      case BREAK -> {
        var label = ((Break) a).label;
        if (label == null) {
          if (context == null) throw new CompileError(a.loc, "break without loop or case");
        } else {
          for (; context != null; context = context.outer) if (label.equals(context.label)) break;
          if (context == null) throw new CompileError(a.loc, label + " not found");
        }
        add(new Goto(a.loc, context.breakTarget));
        block = new Block(a.loc);
      }
      case CONTINUE -> {
        var label = ((Continue) a).label;
        if (label == null) {
          for (; context != null; context = context.outer)
            if (context.continueTarget != null) break;
          if (context == null) throw new CompileError(a.loc, "continue without loop");
        } else {
          for (; context != null; context = context.outer) if (label.equals(context.label)) break;
          if (context == null) throw new CompileError(a.loc, label + " not found");
          if (context.continueTarget == null)
            throw new CompileError(a.loc, label + " is not a loop");
        }
        add(new Goto(a.loc, context.continueTarget));
        block = new Block(a.loc);
      }
      case RETURN -> {
        a.set(0, term(context, a.get(0)));
        add(a);
        block = new Block(a.loc);
      }
      default -> {
        for (var i = 0; i < a.size(); i++) a.set(i, term(context, a.get(i)));
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
