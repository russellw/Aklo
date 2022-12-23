package aklo;

public final class CompileError extends RuntimeException {
  public CompileError(String file, int line, String msg) {
    super(String.format("%s:%d: %s", file, line, msg));
  }

  public CompileError(Loc loc, String msg) {
    super(String.format("%s:%d: %s", loc.file(), loc.line(), msg));
  }
}
