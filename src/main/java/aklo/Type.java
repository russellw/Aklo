package aklo;

public abstract class Type {
  public abstract Kind kind();

  public abstract String descriptor();

  public static final Type BOOL =
      new Type() {
        @Override
        public String descriptor() {
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
        public String descriptor() {
          return "V";
        }
      };
  public static final Type FLOAT =
      new Type() {
        @Override
        public String descriptor() {
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
        public String descriptor() {
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
        public String descriptor() {
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
        public String descriptor() {
          return "Laklo/BigRational;";
        }
      };
}
