package aklo;

import java.util.HashMap;
import java.util.Map;

final class Link {
  private final Link outer;
  private final Map<String, Object> locals = new HashMap<>();
  private String file;
  private int line;

  private Object get(String name) {
    for (var l = this; ; l = l.outer) {
      if (l == null) return null;
      var r = l.locals.get(name);
      if (r != null) return r;
    }
  }

  @SuppressWarnings("ConstantConditions")
  private void link(Instruction a) {
    for (var i = 0; i < a.size(); i++)
      if (a.get(i) instanceof String name) {
        var x = get(name);
        if (x == null) throw new CompileError(file, line, name + " not found");
        a.set(i, x);
      }
    switch (a) {
      case Assign ignored -> {
        if (a.get(0) instanceof Fn)
          throw new CompileError(file, line, a.get(0) + ": assigning a function");
      }
      case Line a1 -> {
        file = a1.file;
        line = a1.line;
      }
      default -> {}
    }
  }

  Link(Link outer, Fn f) {
    this.outer = outer;
    for (var x : f.params) locals.put(x.name, x);
    for (var x : f.vars) locals.put(x.name, x);
    for (var g : f.fns) locals.put(g.name, g);
    for (var g : f.fns) new Link(this, g);
    for (var block : f.blocks) for (var a : block.instructions) link(a);
  }

  @SuppressWarnings("unused")
  private void dbg() {
    System.out.println();
    System.out.println(this);
    for (var l = this; l != null; l = l.outer) System.out.println(l.locals);
    System.out.println();
  }
}
