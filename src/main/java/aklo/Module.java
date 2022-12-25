package aklo;

import java.util.Arrays;

public final class Module extends Fn {
  public final String[] names;

  public Module(Loc loc, String[] names) {
    super(loc, names[names.length - 1]);
    this.names = names;
  }

  @Override
  public String toString() {
    return Arrays.toString(names);
  }
}
