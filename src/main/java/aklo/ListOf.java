package aklo;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ListOf extends Terms {
  public ListOf(Loc loc, List<Term> terms) {
    super(loc, terms);
  }

  public static ListOf encode(Loc loc, String s) {
    return of(loc, s.getBytes(StandardCharsets.UTF_8));
  }

  public static ListOf of(Loc loc, byte[] s) {
    var terms = new Term[s.length];
    for (var i = 0; i < s.length; i++) terms[i] = new ConstInteger(loc, s[i] & 0xff);
    return new ListOf(loc, terms);
  }

  public ListOf(Loc loc, Term[] terms) {
    super(loc, terms);
  }

  @Override
  public Tag tag() {
    return Tag.LIST_OF;
  }
}
