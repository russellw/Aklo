package aklo;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Label;

final class Block {
  final Loc loc;
  String name;
  Label label;
  final List<Term> insns = new ArrayList<>();

  Block(Loc loc, String name) {
    this.loc = loc;
    this.name = name;
  }

  @Override
  String toString() {
    assert name != null;
    return name;
  }

  boolean hasTerminator() {
    var n = insns.size();
    if (n == 0) return false;
    return insns.get(n - 1).isTerminator();
  }
}
