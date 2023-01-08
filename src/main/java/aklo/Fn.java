package aklo;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public class Fn extends Term {
  public final String name;
  public final List<Var> params = new ArrayList<>();
  public Type rtype = Type.ANY;
  public Term body;

  public final List<Var> vars = new ArrayList<>();
  public final List<Block> blocks = new ArrayList<>();

  public Fn(Loc loc, String name) {
    super(loc);
    this.name = name;
  }

  public final void walkFns(Consumer<Fn> f) {
    f.accept(this);
    for (var a : body)
      a.walk(
          b -> {
            if (b instanceof Fn b1) b1.walkFns(f);
          });
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Tag tag() {
    return Tag.FN;
  }

  // convert to basic blocks
  private static final class Env {
    final Env outer;
    final Map<String, Term> locals = new HashMap<>();

    Env(Env outer) {
      this.outer = outer;
    }

    Term get(String name) {
      for (var env = this; env != null; env = env.outer) {
        var a = env.locals.get(name);
        if (a != null) return a;
      }
      return null;
    }
  }

  private record Loop(Fn.Loop outer, String label, Block continueTarget, Block breakTarget) {
    // static because loop could be null
    static Loop get(Loop loop, String label) {
      if (label == null) return loop;
      for (; loop != null; loop = loop.outer) if (label.equals(loop.label)) break;
      return loop;
    }
  }

  private Var var1(Loc loc, String name) {
    var x = new Var(loc, name);
    vars.add(x);
    return x;
  }

  private void addBlock(Block block) {
    blocks.add(block);
  }

  private Block lastBlock() {
    return blocks.get(blocks.size() - 1);
  }

  private void insn(Term a) {
    lastBlock().insns.add(a);
  }

  private void assignSubscript(Env env, Loop loop, Term y, Term x, Block fail, int i) {
    y = y.get(i);
    x = new Subscript(x.loc, x, new Const(x.loc, BigInteger.valueOf(i)));
    insn(x);
    assign(env, loop, y, x, fail);
  }

  private void assign(Env env, Loop loop, Term y, Term x, Block fail) {
    var loc = y.loc;
    // it is possible for y to be a variable
    // i.e. already resolved as a reference to a Var object, instead of still just an Id
    // because of op-assignments
    // y += x
    // desugars to
    // y = y + x
    // this means y occurs twice, and is first resolved on the right-hand side
    // so when the left-hand side is processed, identifiers are already resolved
    switch (y.tag()) {
      case CONST -> {
        var eq = new Eq(loc, y, x);
        insn(eq);
        var after = new Block(loc, "assignCheckAfter");
        insn(new If(loc, eq, after, fail));
        addBlock(after);
      }
      case VAR -> insn(new Assign(loc, y, x));
      case ID -> insn(new Assign(loc, term(env, loop, y), x));
      case LIST_OF -> {
        var n = y.size();
        for (var i = 0; i < n; i++) assignSubscript(env, loop, y, x, fail, i);
      }
      case LIST_REST -> {
        var n = y.size() - 1;
        for (var i = 0; i < n; i++) assignSubscript(env, loop, y, x, fail, i);
        var len = new Len(loc, x);
        insn(len);
        var slice = new Slice(loc, x, new Const(loc, BigInteger.valueOf(n)), len);
        insn(slice);
        assign(env, loop, y.get(n), slice, fail);
      }
      default -> throw new CompileError(loc, y + ": invalid assignment");
    }
  }

  private Term term(Env env, Loop loop, Term a) {
    var r = a;
    switch (a.tag()) {
      case LIST_REST -> {
        var n = a.size() - 1;

        // elements
        var s = new Term[n];
        for (var i = 0; i < n; i++) s[i] = term(env, loop, a.get(i));
        var s1 = new ListOf(a.loc, s);
        insn(s1);

        // rest
        var t = term(env, loop, a.get(n));

        // concatenate
        r = new Cat(a.loc, s1, t);
        insn(r);
      }
      case ASSIGN -> {
        var fail = new Block(a.loc, "assignFail");
        var after = new Block(a.loc, "assignAfter");

        // assign
        r = term(env, loop, a.get(1));
        assign(env, loop, a.get(0), r, fail);
        insn(new Goto(a.loc, after));

        // fail
        addBlock(fail);
        insn(new Throw(loc, new Const(loc, Etc.encode("assign failed"))));

        // after
        addBlock(after);
      }
      case DO -> {
        if (a.isEmpty()) r = new Const(a.loc, BigInteger.ZERO);
        else for (var b : a) r = term(env, loop, b);
      }
      case POST_INC -> {
        r = var1(a.loc, "old");
        insn(new Assign(a.loc, r, term(env, loop, a.get(0))));
      }
      case OR -> {
        r = var1(a.loc, "or");
        var no = new Block(a.loc, "orFalse");
        var after = new Block(a.loc, "orAfter");

        // condition
        insn(new Assign(a.loc, r, term(env, loop, a.get(0))));
        insn(new If(a.loc, r, after, no));

        // false
        addBlock(no);
        insn(new Assign(a.loc, r, term(env, loop, a.get(1))));
        insn(new Goto(a.loc, after));

        // after
        addBlock(after);
      }
      case AND -> {
        r = var1(a.loc, "and");
        var yes = new Block(a.loc, "andTrue");
        var after = new Block(a.loc, "andAfter");

        // condition
        insn(new Assign(a.loc, r, term(env, loop, a.get(0))));
        insn(new If(a.loc, r, yes, after));

        // true
        addBlock(yes);
        insn(new Assign(a.loc, r, term(env, loop, a.get(1))));
        insn(new Goto(a.loc, after));

        // after
        addBlock(after);
      }
      case NOT -> {
        r = var1(a.loc, "not");
        var yes = new Block(a.loc, "notTrue");
        var no = new Block(a.loc, "notFalse");
        var after = new Block(a.loc, "notAfter");

        // condition
        insn(new If(a.loc, term(env, loop, a.get(0)), yes, no));

        // true
        addBlock(yes);
        insn(new Assign(a.loc, r, new Const(a.loc, false)));
        insn(new Goto(a.loc, after));

        // false
        addBlock(no);
        insn(new Assign(a.loc, r, new Const(a.loc, true)));
        insn(new Goto(a.loc, after));

        // after
        addBlock(after);
      }
      case IF -> {
        r = var1(a.loc, "if");
        var yes = new Block(a.loc, "ifTrue");
        var no = new Block(a.loc, "ifFalse");
        var after = new Block(a.loc, "ifAfter");

        // condition
        insn(new If(a.loc, term(env, loop, a.get(0)), yes, no));

        // true
        addBlock(yes);
        insn(new Assign(a.loc, r, term(env, loop, a.get(1))));
        insn(new Goto(a.loc, after));

        // false
        addBlock(no);
        insn(new Assign(a.loc, r, term(env, loop, a.get(2))));
        insn(new Goto(a.loc, after));

        // after
        addBlock(after);
      }
      case WHILE -> {
        var a1 = (While) a;
        var body = new Block(a.loc, "whileBody");
        var cond = new Block(a.loc, "whileCond");
        var after = new Block(a.loc, "whileAfter");
        loop = new Loop(loop, a1.label, cond, after);

        // before
        insn(new Goto(a.loc, a1.doWhile ? body : cond));

        // body
        addBlock(body);
        term(env, loop, a1.arg1);
        insn(new Goto(a.loc, cond));

        // condition
        addBlock(cond);
        insn(new If(a.loc, term(env, loop, a1.arg0), body, after));

        // after
        addBlock(after);
        return new Const(a.loc, BigInteger.ZERO);
      }
      case GOTO -> {
        var a1 = (ContinueBreak) a;
        var label = a1.label;
        loop = Loop.get(loop, label);
        if (loop == null) {
          if (label == null) {
            var s = a1.break1 ? "break" : "continue";
            throw new CompileError(a.loc, s + " without loop");
          }
          throw new CompileError(a.loc, label + " not found");
        }
        insn(new Goto(a.loc, a1.break1 ? loop.breakTarget : loop.continueTarget));
        addBlock(new Block(a.loc, "gotoAfter"));
        return new Const(a.loc, BigInteger.ZERO);
      }
      case RETURN, THROW -> {
        a.set(0, term(env, loop, a.get(0)));
        insn(a);
        addBlock(new Block(a.loc, "after"));
        return new Const(a.loc, BigInteger.ZERO);
      }
      case ID -> {
        var s = a.toString();
        r = env.get(s);
        if (r == null) throw new CompileError(a.loc, s + " not found");
      }
      case CONST -> {}
      default -> {
        for (var i = 0; i < a.size(); i++) a.set(i, term(env, loop, a.get(i)));
        insn(a);
      }
    }
    return r;
  }

  private void toBlocks(Env outer) {
    // environment of local variables and functions
    var env = new Env(outer);
    for (var x : vars) env.locals.put(x.name, x);
    for (var a : body)
      a.walk(
          b -> {
            if (b instanceof Fn b1) env.locals.put(b1.name, b);
          });

    // convert nested functions to basic blocks
    for (var a : body)
      a.walk(
          b -> {
            if (b instanceof Fn b1) b1.toBlocks(env);
          });

    // convert this function to basic blocks
    addBlock(new Block(loc, "entry"));
    insn(new Return(loc, term(env, null, body)));
  }

  public void toBlocks() {
    toBlocks(null);
  }

  // debug output
  public void dbg() {
    // make local names unique
    var names = new HashSet<String>();
    for (var block : blocks) {
      if (names.add(block.name)) continue;
      for (var i = 1; ; i++) {
        var s = block.name + i;
        if (names.add(s)) {
          block.name = s;
          break;
        }
      }
    }

    names = new HashSet<>();
    for (var x : vars) {
      if (names.add(x.name)) continue;
      for (var i = 1; ; i++) {
        var s = x.name + i;
        if (names.add(s)) {
          x.name = s;
          break;
        }
      }
    }

    // header
    System.out.printf("fn %s(", name);
    for (var i = 0; i < params.size(); i++) {
      if (i > 0) System.out.print(", ");
      System.out.print(params.get(i).name);
    }
    System.out.println(')');

    // local variables
    for (var x : vars) System.out.printf("  var %s %s\n", x, x.type);

    // which instructions are used as input to others, therefore needing reference numbers?
    var used = new HashSet<Term>();
    for (var block : blocks) for (var a : block.insns) used.addAll(a);

    // assign reference numbers to instructions
    var refs = new HashMap<Term, Integer>();
    for (var block : blocks)
      for (var a : block.insns) if (used.contains(a)) refs.put(a, refs.size());

    // blocks
    for (var block : blocks) {
      if (block.name != null) System.out.printf("  %s:\n", block.name);
      for (var a : block.insns) {
        System.out.print("    ");
        var r = refs.get(a);
        if (r != null) System.out.printf("%%%d = ", r);
        a.dbg(refs);
        System.out.println();
      }
    }
  }
}
