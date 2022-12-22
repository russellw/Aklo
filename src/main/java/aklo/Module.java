package aklo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Module {
  public final String[] name;
  public final List<Term> body = new ArrayList<>();

  public Module(String[] name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return Arrays.toString(name);
  }
}
