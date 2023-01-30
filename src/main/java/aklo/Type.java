package aklo;

import static org.objectweb.asm.Opcodes.*;

class Type extends Named {
  static final Type VOID = new Type("V");
  static final Type BOOL_PRIM = new Type("Z");
  static final Type FLOAT_PRIM = new Type("F");
  static final Type DOUBLE_PRIM = new Type("D");
  static final Type INTEGER = new Type("java/math/BigInteger");
  static final Type RATIONAL = new Type("aklo/BigRational");
  static final Type SYM = new Type("aklo/Sym");
  static final Type LIST = new Type("java/util/List");
  static final Type STRING = new Type("java/lang/String");
  static final Type BOOL = new Type("java/lang/Boolean");
  static final Type FLOAT = new Type("java/lang/Float");
  static final Type DOUBLE = new Type("java/lang/Double");
  static final Type OBJECT = new Type("java/lang/Object");

  @Override
  public String toString() {
    if (name.length() == 1) return name;
    return 'L' + name + ';';
  }

  Type(String name) {
    super(name);
  }

  public static Type of(String descriptor) {
    return switch (descriptor) {
      case "V" -> VOID;
      case "F" -> FLOAT_PRIM;
      case "D" -> DOUBLE_PRIM;
      case "Z" -> BOOL_PRIM;
      case "Laklo/BigRational;" -> RATIONAL;
      case "Laklo/Sym;" -> SYM;
      case "Ljava/math/BigInteger;" -> INTEGER;
      case "Ljava/util/List;" -> LIST;
      case "Ljava/lang/Boolean;" -> BOOL;
      default -> {
        if (descriptor.startsWith("Ljava/util/ImmutableCollections$List")) yield LIST;
        throw new IllegalArgumentException(descriptor);
      }
    };
  }
}
