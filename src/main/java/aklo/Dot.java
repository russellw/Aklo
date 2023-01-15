package aklo;

import java.util.List;

final class Dot extends Insn {
  final String[] names;

  Dot(Loc loc, List<String> names) {
    super(loc);
    this.names = names.toArray(new String[0]);
  }

  @Override
  Tag tag() {
    return Tag.DOT;
  }
}
