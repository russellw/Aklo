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

  private static void check(Instruction a, boolean cond) {
    if (!cond)
      throw new IllegalStateException(
          String.format("%s:%d: %s: %s: verify failed", file, line, fname, a));
  }

  static void verify() {
    for (var f : Program.fns) {
      fname = f.name;
      for (var block : f.blocks) {
        check(!block.instructions.isEmpty());
        for (var a : block.instructions)
          if (a instanceof Line a1) {
            file = a1.file;
            line = a1.line;
          }
        check(block.last().isTerminator());
      }

      // Is every instruction in a block?
      var found = new HashSet<Instruction>();
      for (var block : f.blocks) found.addAll(block.instructions);
      for (var block : f.blocks)
        for (var a : block.instructions) {
          if (a instanceof Line a1) {
            file = a1.file;
            line = a1.line;
          }
          for (var b : a) if (b instanceof Instruction b1) check(b1, found.contains(b1));
        }
    }
  }
}
