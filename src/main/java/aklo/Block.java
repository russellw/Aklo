package aklo;

import java.util.ArrayList;
import java.util.List;

public final class Block {
  public final Loc loc;
  public final String name;
  public final List<Term> insns = new ArrayList<>();

  public Block(Loc loc, String name) {
    this.loc = loc;
    this.name = name;
  }

  @Override
  public String toString() {
    if (name != null) return name;
    return "#" + hashCode();
  }

  public boolean hasTerminator() {
    var n = insns.size();
    if (n == 0) return false;
    return insns.get(n - 1).isTerminator();
  }
}
