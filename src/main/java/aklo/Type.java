package aklo;

public abstract class Type {
  public static final Type BOOL =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/Boolean";
        }

        @Override
        public Kind kind() {
          return Kind.BOOL;
        }
      };
  public static final Type VOID =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.VOID;
        }

        @Override
        public String toString() {
          return "V";
        }
      };
  public static final Type FLOAT =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/Float";
        }

        @Override
        public Kind kind() {
          return Kind.FLOAT;
        }
      };
  public static final Type DOUBLE =
      new Type() {
        @Override
        public String toString() {
          return "java/lang/Double";
        }

        @Override
        public Kind kind() {
          return Kind.DOUBLE;
        }
      };
  public static final Type INT =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.INT;
        }

        @Override
        public String toString() {
          return "java/math/BigInteger";
        }
      };
  public static final Type RAT =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.RAT;
        }

        @Override
        public String toString() {
          return "aklo/BigRational";
        }
      };
  public static final Type ANY =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.ANY;
        }

        @Override
        public String toString() {
          return "java/lang/Object";
        }
      };
  public static final Type LIST =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.LIST;
        }

        @Override
        public String toString() {
          return "java/util/List";
        }
      };
  public static final Type SYM =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.SYM;
        }

        @Override
        public String toString() {
          return "aklo/Sym";
        }
      };

  public abstract Kind kind();

  public final String descriptor() {
    var s = toString();
    if (s.length() > 1) s = 'L' + s + ';';
    return s;
  }

  public static Type of(String s) {
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
