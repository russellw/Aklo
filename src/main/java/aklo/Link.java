package aklo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Link {
  static final Map<List<String>, Fn> modules = new LinkedHashMap<>();

  private final Link outer;
  private final Map<String, Object> locals;

  // TODO refactor
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
      case Loc a1 -> {
        file = a1.file;
        line = a1.line;
      }
      default -> {}
    }
  }

  private Link(Map<String, Object> globals, Fn module) {
    outer = null;
    locals = globals;
    new Link(this, module);
  }

  private Link(Link outer, Fn f) {
    this.outer = outer;
    locals = new HashMap<>();
    for (var x : f.params) locals.put(x.name, x);
    for (var x : f.vars) locals.put(x.name, x);
    for (var g : f.fns) locals.put(g.name, g);
    for (var g : f.fns) new Link(this, g);
    for (var block : f.blocks) for (var a : block.instructions) link(a);
  }

  static void link() {
    var ubiquitous = modules.get(List.of("aklo", "ubiquitous"));
    var globals = new HashMap<String, Object>();
    for (var g : ubiquitous.fns) globals.put(g.name, g);

    for (var module : Link.modules.values()) new Link(globals, module);
  }

  @SuppressWarnings("unused")
  private void dbg() {
    System.out.println();
    System.out.println(this);
    for (var l = this; l != null; l = l.outer) System.out.println(l.locals);
    System.out.println();
  }
}
