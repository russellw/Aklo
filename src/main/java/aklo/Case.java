package aklo;

import java.util.List;

final class Case extends Nary {
  Case(List<Object> terms) {
    super(null, terms);
  }

  @Override
  Tag tag() {
    return Tag.CASE;
  }
}
