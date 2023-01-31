package aklo;

import java.util.HashMap;
import java.util.Map;

public final class Env {
  final Env outer;
  final Map<Sym, Var> locals = new HashMap<>();

  public Env(Env outer) {
    this.outer = outer;
  }
}
