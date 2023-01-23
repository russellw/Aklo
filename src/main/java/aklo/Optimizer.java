package aklo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

final class Optimizer {
  private static boolean changed;

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

  private static void redundantLoc(Block block) {
    var s = block.instructions;
    var r = new ArrayList<Instruction>();
    for (var i = 0; i < s.size(); i++) {
      if (i < s.size() - 1 && s.get(i) instanceof Loc && s.get(i + 1) instanceof Loc) continue;
      r.add(s.get(i));
    }
    block.instructions = r;
  }

  private static void deadCode(Fn f) {
    var visited = new HashSet<Block>();
    mark(f.blocks.get(0), visited);
    var r = new ArrayList<Block>();
    for (var block : f.blocks)
      if (visited.contains(block)) r.add(block);
      else changed = true;
    f.blocks = r;
  }

  static void optimize() {
    do {
      changed = false;
      for (var f : Program.fns) {
        deadCode(f);
        for (var block : f.blocks) redundantLoc(block);
      }
    } while (changed);
  }
}
