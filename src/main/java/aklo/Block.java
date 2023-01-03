package aklo;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Label;

public final class Block {
  public final Loc loc;
  public String name;
  public Label label;
  public final List<Term> insns = new ArrayList<>();

  public Block(Loc loc, String name) {
    this.loc = loc;
    this.name = name;
  }

  @Override
  public String toString() {
    assert name != null;
    return name;
  }

  public boolean hasTerminator() {
    var n = insns.size();
    if (n == 0) return false;
    return insns.get(n - 1).isTerminator();
  }
}
