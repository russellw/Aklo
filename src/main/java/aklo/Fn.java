package aklo;

import java.util.*;

public final class Fn extends Term {
  public final String name;
  public final List<Var> params = new ArrayList<>();
  public Type rtype = Type.ANY;
  public final List<Var> vars = new ArrayList<>();
  public final List<Fn> fns = new ArrayList<>();
  public final List<Block> blocks = new ArrayList<>();

  public Fn(Loc loc, String name) {
    super(loc);
    this.name = name;
    addBlock(new Block(loc, "entry"));
  }

  public void initVars() {
    var r = new ArrayList<Term>();
    for (var x : vars) r.add(new Assign(loc, x, Const.ZERO));
    blocks.get(0).insns.addAll(0, r);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Tag tag() {
    return Tag.FN;
  }

  public String descriptor() {
    var sb = new StringBuilder("(");
    for (var x : params) sb.append(x.type.descriptor());
    sb.append(')');
    sb.append(rtype.descriptor());
    return sb.toString();
  }

  private void addBlock(Block block) {
    blocks.add(block);
  }

  public void dbg() {
    // make block names unique
    var names = new HashSet<String>();
    for (var block : blocks) {
      if (names.add(block.name)) continue;
      for (var i = 1; ; i++) {
        var s = block.name + i;
        if (names.add(s)) {
          block.name = s;
          break;
        }
      }
    }

    // header
    System.out.printf("fn %s(", name);
    for (var i = 0; i < params.size(); i++) {
      if (i > 0) System.out.print(", ");
      System.out.print(params.get(i));
    }
    System.out.println(')');

    // local variables
    for (var x : vars) System.out.printf("  var %s %s\n", x, x.type);

    // which instructions are used as input to others, therefore needing reference numbers?
    var used = new HashSet<Term>();
    for (var block : blocks) for (var a : block.insns) used.addAll(a);

    // assign reference numbers to instructions
    var refs = new HashMap<Term, Integer>();
    for (var block : blocks)
      for (var a : block.insns) if (used.contains(a)) refs.put(a, refs.size());

    // blocks
    for (var block : blocks) {
      if (block.name != null) System.out.printf("  %s:\n", block.name);
      for (var a : block.insns) {
        System.out.print("    ");
        var r = refs.get(a);
        if (r != null) System.out.printf("%%%d = ", r);
        a.dbg(refs);
        System.out.println();
      }
    }
  }
}
