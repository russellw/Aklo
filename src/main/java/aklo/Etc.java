package aklo;

public final class Etc {
  private Etc() {}

  public static void dbg(Object a) {
    System.out.printf("%s: %s\n", Thread.currentThread().getStackTrace()[2], a);
  }

  public static boolean isDigit(int c) {
    return '0' <= c && c <= '9';
  }

  public static boolean isUpper(int c) {
    return 'A' <= c && c <= 'Z';
  }

  public static boolean isAlpha(int c) {
    return isLower(c) || isUpper(c);
  }

  public static boolean isAlnum(int c) {
    return isAlpha(c) || isDigit(c);
  }

  public static boolean isLower(int c) {
    return 'a' <= c && c <= 'z';
  }
}
