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
    addBlock(new Block(loc, "entry"));
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

  private Var mkVar(Loc loc) {
    var x = new Var(loc);
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
    switch (y.tag()) {
      case CONST -> {
        var eq = new Eq(loc, y, x);
        insn(eq);
        var after = new Block(loc, "assignCheckAfter");
        insn(new If(loc, eq, after, fail));
        addBlock(after);
      }
      case ID, VAR -> insn(new Assign(loc, term(env, loop, y), x));
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
      case ID -> {
        var s = a.toString();
        r = env.get(s);
        if (r == null) throw new CompileError(a.loc, s + " not found");
      }
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
    for (var x : vars) if (x.name != null) env.locals.put(x.name, x);
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
    // make block names unique
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

    // header
    System.out.printf("fn %s(", name);
    for (var i = 0; i < params.size(); i++) {
      if (i > 0) System.out.print(", ");
      System.out.print(params.get(i));
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
