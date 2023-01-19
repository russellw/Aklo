package aklo;

import java.util.Map;
import org.objectweb.asm.MethodVisitor;

final class Line extends Instruction {
  final int line;

  @Override
  void emit(Map<Object, Integer> refs, MethodVisitor mv) {}

  @Override
  void dbg(Map<Object, Integer> refs) {
    System.out.print("Line " + line);
  }

  Line(int line) {
    this.line = line;
  }
}
