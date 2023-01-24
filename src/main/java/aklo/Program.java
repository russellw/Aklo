package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;

final class Program {
  static final List<Var> vars = new ArrayList<>();
  static final List<Fn> fns = new ArrayList<>();

  private static void lift(Fn f) {
    for (var g : f.fns) lift(g);
    fns.add(f);
  }

  static void init(Collection<Fn> modules) {
    var main = new Fn("main");
    var args = new Var("args", main.params);
    args.type = "[Ljava/lang/String;";
    main.rtype = "V";
    for (var module : modules) {
      // lift functions to global scope
      lift(module);

      // Module scope variables are static
      vars.addAll(module.vars);
      module.vars.clear();

      // modules may contain initialization code
      // so each module is called as a function from main
      main.lastBlock().instructions.add(new Call(module));
    }
    main.lastBlock().instructions.add(new ReturnVoid());
    fns.add(main);
  }
}
