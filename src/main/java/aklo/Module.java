package aklo;

import java.util.Arrays;

public final class Module extends Fn {
  public final String[] modName;

  public Module(String[] modName) {
    super(new Loc(modName[modName.length - 1], 1));
    this.modName = modName;
  }

  @Override
  public String toString() {
    return Arrays.toString(modName);
  }
}
