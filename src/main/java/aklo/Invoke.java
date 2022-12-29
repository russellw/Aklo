package aklo;

import java.util.List;

public final class Invoke extends Terms {
  public final int opcode;
  public final String owner;
  public final String name;
  public final String descriptor;

  public Invoke(
      Loc loc, int opcode, String owner, String name, String descriptor, List<Term> terms) {
    super(loc, terms);
    this.opcode = opcode;
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public Tag tag() {
    return Tag.INVOKE;
  }

  @Override
  public Term remake(Loc loc, Term[] terms) {
    return null;
  }
}
