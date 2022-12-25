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
  private final List<Block> blocks = new ArrayList<>();
  private Fn fn;

  private void block(Block block) {
    blocks.add(block);
  }

  private void add(Term a) {
    blocks.get(blocks.size() - 1).insns.add(a);
  }

  private Term term(Context context, Term a) {
    switch (a.tag()) {
      case OR -> {
        var r = new Var(a.loc, "or");
        fn.vars.add(r);
        var falseBlock = new Block(a.loc);
        var afterBlock = new Block(a.loc);

        // condition
        add(new Def(a.loc, r, term(context, a.get(0))));
        add(new If(a.loc, r, afterBlock, falseBlock));

        // false
        block(falseBlock);
        add(new Def(a.loc, r, term(context, a.get(1))));
        add(new Goto(a.loc, afterBlock));

        // after
        block(afterBlock);
        return r;
      }
      case AND -> {
        var r = new Var(a.loc, "and");
        fn.vars.add(r);
        var trueBlock = new Block(a.loc);
        var afterBlock = new Block(a.loc);

        // condition
        add(new Def(a.loc, r, term(context, a.get(0))));
        add(new If(a.loc, r, trueBlock, afterBlock));

        // true
        block(trueBlock);
        add(new Def(a.loc, r, term(context, a.get(1))));
        add(new Goto(a.loc, afterBlock));

        // after
        block(afterBlock);
        return r;
      }
      case NOT -> {
        var r = new Var(a.loc, "not");
        fn.vars.add(r);
        var trueBlock = new Block(a.loc);
        var falseBlock = new Block(a.loc);
        var afterBlock = new Block(a.loc);

        // condition
        add(new If(a.loc, term(context, a.get(0)), trueBlock, falseBlock));

        // true
        block(trueBlock);
        add(new Def(a.loc, r, new False(a.loc)));
        add(new Goto(a.loc, afterBlock));

        // false
        block(falseBlock);
        add(new Def(a.loc, r, new True(a.loc)));
        add(new Goto(a.loc, afterBlock));

        // after
        block(afterBlock);
        return r;
      }
      case IF -> {
        var a1 = (IfStmt) a;
        var trueBlock = new Block(a.loc);
        var falseBlock = new Block(a.loc);
        var afterBlock = new Block(a.loc);

        // condition
        add(new If(a.loc, term(context, a1.get(0)), trueBlock, falseBlock));

        // true
        block(trueBlock);
        for (var i = 1; i < a1.elseIdx; i++) term(context, a1.get(i));
        add(new Goto(a.loc, afterBlock));

        // false
        block(falseBlock);
        for (var i = a1.elseIdx; i < a1.size(); i++) term(context, a1.get(i));
        add(new Goto(a.loc, afterBlock));

        // after
        block(afterBlock);
      }
      case WHILE -> {
        var a1 = (While) a;
        var bodyBlock = new Block(a.loc);
        var condBlock = new Block(a.loc);
        var afterBlock = new Block(a.loc);
        context = new Context(context, a1.label, condBlock, afterBlock);

        // before
        add(new Goto(a.loc, a1.doWhile ? bodyBlock : condBlock));

        // body
        block(bodyBlock);
        for (var i = 1; i < a1.size(); i++) term(context, a1.get(i));
        add(new Goto(a.loc, condBlock));

        // condition
        block(condBlock);
        add(new If(a.loc, term(context, a1.get(0)), bodyBlock, afterBlock));

        // after
        block(afterBlock);
      }
      case GOTO -> {
        var a1 = (ContinueBreak) a;
        var label = a1.label;
        if (label == null) {
          if (context == null) {
            var s = a1.break1 ? "break" : "continue";
            throw new CompileError(a.loc, s + " without loop");
          }
        } else {
          for (; context != null; context = context.outer) if (label.equals(context.label)) break;
          if (context == null) throw new CompileError(a.loc, label + " not found");
        }
        add(new Goto(a.loc, a1.break1 ? context.breakTarget : context.continueTarget));
        block(new Block(a.loc));
      }
      case RETURN -> {
        a.set(0, term(context, a.get(0)));
        add(a);
        block(new Block(a.loc));
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
