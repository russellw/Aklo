package aklo;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Loc extends Instruction {
  final String file;
  final int line;

  @Override
  public String toString() {
    return String.format("Loc %s %d", file, line);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {}

  Loc(String file, int line) {
    this.file = file;
    this.line = line;
  }
}
