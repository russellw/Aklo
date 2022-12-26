package aklo;

import java.util.ArrayList;
import java.util.List;

public final class Block {
  public final Loc loc;
  public final List<Term> insns = new ArrayList<>();

  public Block(Loc loc) {
    this.loc = loc;
  }

  public boolean hasTerminator() {
    var n = insns.size();
    if (n == 0) return false;
    return insns.get(n - 1).isTerminator();
  }
}
