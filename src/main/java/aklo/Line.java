package aklo;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Line extends Instruction {
  final String file;
  final int line;

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {}

  @Override
  void dbg(Map<Object, Integer> refs) {
    System.out.printf("Line %s %d", file, line);
  }

  Line(String file, int line) {
    this.file = file;
    this.line = line;
  }
}
