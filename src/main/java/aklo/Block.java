package aklo;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Label;

final class Block {
  String name;
  Label label;
  final List<Instruction> instructions = new ArrayList<>();

  Block(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    assert name != null;
    return name;
  }

  boolean hasTerminator() {
    var n = instructions.size();
    if (n == 0) return false;
    return instructions.get(n - 1).isTerminator();
  }
}
