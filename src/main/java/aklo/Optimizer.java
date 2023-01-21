package aklo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

final class Optimizer {
  private static void mark(Block block, Set<Block> visited) {
    if (!visited.add(block)) return;
    var a = block.last();
    switch (a) {
      case Goto a1 -> mark(a1.target, visited);
      case If a1 -> {
        mark(a1.trueTarget, visited);
        mark(a1.falseTarget, visited);
      }
      default -> {}
    }
  }

  private static void deadCode(Fn f) {
    var visited = new HashSet<Block>();
    mark(f.blocks.get(0), visited);
    var r = new ArrayList<Block>();
    for (var block : f.blocks) if (visited.contains(block)) r.add(block);
    f.blocks = r;
  }

  static void optimize() {
    for (var f : Program.fns) deadCode(f);
  }
}
