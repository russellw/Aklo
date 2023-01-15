package aklo;

record Loc(String file, int line) {
  @Override
  public String toString() {
    return String.format("%s:%d", file, line);
  }
}
