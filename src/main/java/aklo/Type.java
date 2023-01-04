package aklo;

public abstract class Type {
  public static final Type BOOL =
      new Type() {
        @Override
        public String toString() {
          return "Z";
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
          return "F";
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
          return "D";
        }

        @Override
        public Kind kind() {
          return Kind.DOUBLE;
        }
      };
  public static final Type INTEGER =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.INTEGER;
        }

        @Override
        public String toString() {
          return "Ljava/math/BigInteger;";
        }
      };
  public static final Type RATIONAL =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.RATIONAL;
        }

        @Override
        public String toString() {
          return "Laklo/BigRational;";
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
          return "Ljava/lang/Object;";
        }
      };

  public abstract Kind kind();

  public static Type of(String descriptor) {
    return switch (descriptor) {
      case "V" -> VOID;
      case "F" -> FLOAT;
      case "D" -> DOUBLE;
      case "Z" -> BOOL;
      case "Laklo/BigRational;" -> RATIONAL;
      case "Ljava/math/BigInteger;" -> INTEGER;
      default -> ANY;
    };
  }
}
