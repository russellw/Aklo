package aklo;

import java.util.HashSet;

class Verifier {
  private static String file;
  private static int line;
  private static String fname;

  private static void check(boolean cond) {
    if (!cond)
      throw new IllegalStateException(String.format("%s:%d: %s: verify failed", file, line, fname));
  }

  private static void line(Instruction a) {
    // TODO refactor
    if (a instanceof Loc a1) {
      file = a1.file;
      line = a1.line;
    }
  }

  private static void check(Instruction a, boolean cond) {
    if (!cond)
      throw new IllegalStateException(
          String.format("%s:%d: %s: %s: verify failed", file, line, fname, a));
  }

  static void verify() {
    for (var c : Type.classes)
      for (var f : c.fns) {
        fname = f.name;

        // every block has exactly one terminator instruction, at the end
        for (var block : f.blocks) {
          check(!block.instructions.isEmpty());
          for (var i = 0; i < block.instructions.size() - 1; i++) {
            var a = block.instructions.get(i);
            line(a);
            check(a, !a.isTerminator());
          }
          check(block.last().isTerminator());
        }

        // every instruction referred to, is found in one of the blocks
        var found = new HashSet<Instruction>();
        for (var block : f.blocks) found.addAll(block.instructions);
        for (var block : f.blocks)
          for (var a : block.instructions) {
            line(a);
            for (var b : a) if (b instanceof Instruction b1) check(b1, found.contains(b1));
          }
      }
  }
}
