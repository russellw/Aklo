package aklo;

import java.util.*;

public final class Program {
  public final List<Var> vars = new ArrayList<>();
  public final List<Fn> fns = new ArrayList<>();

  public Program(List<Module> modules) {
    for (var module : modules) module.toBlocks();
  }
}
