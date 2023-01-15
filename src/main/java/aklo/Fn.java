package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

final class Fn {
  final String name;
  final List<Var> params = new ArrayList<>();
  String rtype = "Ljava/lang/Object;";
  final List<Var> vars = new ArrayList<>();
  final List<Fn> fns = new ArrayList<>();
  final List<Block> blocks = new ArrayList<>();

  Fn(Loc loc, String name) {
    this.name = name;
    addBlock(new Block(loc, "entry"));
  }

  private static int wordSize(String type) {
    return 1;
  }

  private Map<Object, Integer> refs() {
    var i = 0;
    var r = new HashMap<Object, Integer>();

    // assign reference numbers to variables
    for (var x : params) {
      r.put(x, i);
      i += wordSize(x.type);
    }
    for (var x : vars) {
      r.put(x, i);
      i += wordSize(x.type);
    }

    // which instructions are used as input to others, therefore needing reference numbers?
    var used = new HashSet<Term>();
    for (var block : blocks)
      for (var a : block.insns) for (var b : a) if (b instanceof Term b1) used.add(b1);

    // assign reference numbers to instructions
    for (var block : blocks)
      for (var a : block.insns)
        if (used.contains(a)) {
          r.put(a, i);
          i += wordSize(a.type());
        }

    return r;
  }

  void write(ClassWriter w) {
    var refs = refs();

    // label blocks
    for (var block : blocks) block.label = new Label();

    // emit code
    var mv = w.visitMethod(ACC_PUBLIC | ACC_STATIC, name, descriptor(), null, null);
    mv.visitCode();
    for (var block : blocks) {
      mv.visitLabel(block.label);
      for (var a : block.insns) {
        a.emit(refs, mv);
        var i = refs.get(a);
        if (i == null)
          switch (a.type()) {
            case "V" -> {}
            default -> mv.visitInsn(POP);
          }
        else mv.visitVarInsn(ASTORE, i);
      }
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  void initVars() {
    var r = new ArrayList<Term>();
    for (var x : vars) r.add(new Assign(blocks.get(0).loc, x, Const.ZERO));
    blocks.get(0).insns.addAll(0, r);
  }

  @Override
  public String toString() {
    return name;
  }

  String descriptor() {
    var sb = new StringBuilder("(");
    for (var x : params) sb.append(x.type);
    sb.append(')');
    sb.append(rtype);
    return sb.toString();
  }

  private void addBlock(Block block) {
    blocks.add(block);
  }

  @SuppressWarnings("unused")
  void dbg() {
    var refs = refs();

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
