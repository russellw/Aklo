package aklo;

import java.util.ArrayList;
import java.util.List;

public final class Block {
  public final String name;
  public final List<Term> insts = new ArrayList<>();

  public Block(String name) {
    this.name = name;
  }
}
