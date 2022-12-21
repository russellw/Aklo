package aklo;

import static org.junit.Assert.*;

import org.junit.Test;

public class EtcTest {
  @Test
  public void unesc() {
    assertEquals(Etc.unesc(""), "");
    assertEquals(Etc.unesc("abc"), "abc");
    assertEquals(Etc.unesc("\\n"), "\n");
    assertEquals(Etc.unesc("\\xa"), "\n");
    assertEquals(Etc.unesc("\\u000d\\u000a"), "\r\n");
    assertEquals(Etc.unesc("\\\\"), "\\");
    assertEquals(Etc.unesc("\\"), "\\");
  }
}
