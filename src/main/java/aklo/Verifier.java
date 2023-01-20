package aklo;

class Verifier {
  private static String file;
  private static int line;
  private static String fname;

  private static void check(boolean cond) {
    if (!cond)
      throw new IllegalStateException(String.format("%s:%d: %s: verify failed", file, line, fname));
  }

  static void verify() {
    for (var f : Program.fns) {
      fname = f.name;
      for (var block : f.blocks) {
        check(!block.instructions.isEmpty());
        for (var a : block.instructions) {
          if (a instanceof Line a1) {
            file = a1.file;
            line = a1.line;
            continue;
          }
        }
        check(block.last().isTerminator());
      }
    }
  }
}
