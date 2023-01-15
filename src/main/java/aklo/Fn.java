package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

final class Fn extends Term {
  final String name;
  final List<Var> params = new ArrayList<>();
  Type rtype = Type.ANY;
  final List<Var> vars = new ArrayList<>();
  final List<Fn> fns = new ArrayList<>();
  final List<Block> blocks = new ArrayList<>();

  Fn(Loc loc, String name) {
    super(loc);
    this.name = name;
    addBlock(new Block(loc, "entry"));
  }

  void write(ClassWriter w) {
    // label blocks
    for (var block : blocks) block.label = new Label();

    // assign local variable numbers
    var i = 0;
    for (var x : params) x.localVar = i++;
    for (var x : vars) x.localVar = i++;
    for (var block : blocks)
      for (var a : block.insns)
        switch (a.type().kind()) {
          case VOID -> {}
          case DOUBLE -> {
            // TODO long
            a.localVar = i;
            i += 2;
          }
          default -> a.localVar = i++;
        }

    // emit code
    var mv = w.visitMethod(ACC_PUBLIC | ACC_STATIC, name, descriptor(), null, null);
    mv.visitCode();
    for (var block : blocks) {
      mv.visitLabel(block.label);
      for (var a : block.insns) {
        a.emit(mv);
        switch (a.type().kind()) {
          case VOID -> {}
            // case DOUBLE -> mv.visitVarInsn(DSTORE, a.localVar);
          default -> mv.visitVarInsn(ASTORE, a.localVar);
        }
      }
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  void initVars() {
    var r = new ArrayList<Term>();
    for (var x : vars) r.add(new Assign(loc, x, Const.ZERO));
    blocks.get(0).insns.addAll(0, r);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  Tag tag() {
    return Tag.FN;
  }

  String descriptor() {
    var sb = new StringBuilder("(");
    for (var x : params) sb.append(x.type.descriptor());
    sb.append(')');
    sb.append(rtype.descriptor());
    return sb.toString();
  }

  private void addBlock(Block block) {
    blocks.add(block);
  }

  @SuppressWarnings("unused")
  void dbg() {
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
