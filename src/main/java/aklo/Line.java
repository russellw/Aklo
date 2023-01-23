package aklo;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Line extends Instruction {
  final String file;
  final int line;

  @Override
  public String toString() {
    return String.format("Line %s %d", file, line);
  }

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {}

  Line(String file, int line) {
    this.file = file;
    this.line = line;
  }
}
