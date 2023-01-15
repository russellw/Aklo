package aklo;

import java.util.AbstractList;

abstract class Type extends AbstractList<Type> {
  static final Type BOOL =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/Boolean";
        }

        @Override
        Kind kind() {
          return Kind.BOOL;
        }
      };
  static final Type STRING =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/String";
        }

        @Override
        Kind kind() {
          return Kind.STRING;
        }
      };
  static final Type VOID =
      new Type() {
        @Override
        Kind kind() {
          return Kind.VOID;
        }

        @Override
        public String toString() {
          return "V";
        }
      };
  static final Type FLOAT =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/Float";
        }

        @Override
        Kind kind() {
          return Kind.FLOAT;
        }
      };
  static final Type DOUBLE =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/Double";
        }

        @Override
        Kind kind() {
          return Kind.DOUBLE;
        }
      };
  static final Type INT =
      new Type() {
        @Override
        Kind kind() {
          return Kind.INT;
        }

        @Override
        public String toString() {
          return "java/math/BigInteger";
        }
      };
  static final Type RAT =
      new Type() {
        @Override
        Kind kind() {
          return Kind.RAT;
        }

        @Override
        public String toString() {
          return "aklo/BigRational";
        }
      };
  static final Type ANY =
      new Type() {
        @Override
        Kind kind() {
          return Kind.ANY;
        }

        @Override
        public String toString() {
          return "java/lang/Object";
        }
      };
  static final Type LIST =
      new Type() {
        @Override
        Kind kind() {
          return Kind.LIST;
        }

        @Override
        public String toString() {
          return "java/util/List";
        }
      };
  static final Type SYM =
      new Type() {
        @Override
        Kind kind() {
          return Kind.SYM;
        }

        @Override
        public String toString() {
          return "aklo/Sym";
        }
      };

  abstract Kind kind();

  @Override
  public Type get(int i) {
    throw new UnsupportedOperationException(toString());
  }

  @Override
  public int size() {
    return 0;
  }
  int wordSize(){
      return 1;
  }

  String descriptor() {
    var s = toString();
    if (s.length() > 1) s = 'L' + s + ';';
    return s;
  }

  static Type of(String s) {
    return switch (s) {
      case "V" -> VOID;
      case "java/lang/Float" -> FLOAT;
      case "java/lang/Double" -> DOUBLE;
      case "java/lang/Boolean" -> BOOL;
      case "aklo/BigRational" -> RAT;
      case "java/math/BigInteger" -> INT;
      case "java/util/List" -> LIST;
      case "java/lang/Object" -> ANY;
      case "aklo/Sym" -> SYM;
      default -> throw new IllegalArgumentException(s);
    };
  }
}
