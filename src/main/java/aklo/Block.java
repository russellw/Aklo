package aklo;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Label;

final class Block extends Named {
  Label label;
  List<Instruction> instructions = new ArrayList<>();

  Block(String name) {
    super(name);
  }

  Instruction last() {
    return instructions.get(instructions.size() - 1);
  }

  @Override
  public String toString() {
    assert name != null;
    return name;
  }
}
