package aklo;

public final class ParseError extends RuntimeException {
  public ParseError(String message) {
    super(message);
  }
}
