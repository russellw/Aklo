package aklo;

public abstract class Type {
  public abstract Kind kind();

  public static final Type BOOL =
      new Type() {
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
      };
  public static final Type FLOAT =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.FLOAT;
        }
      };
  public static final Type DOUBLE =
      new Type() {
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
      };
  public static final Type RATIONAL =
      new Type() {
        @Override
        public Kind kind() {
          return Kind.RATIONAL;
        }
      };
}
