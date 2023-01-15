package aklo;

final class CompileError extends RuntimeException {
  CompileError(String file, int line, String msg) {
    super(String.format("%s:%d: %s", file, line, msg));
  }

  CompileError(Loc loc, String msg) {
    super(String.format("%s:%d: %s", loc.file(), loc.line(), msg));
  }
}
