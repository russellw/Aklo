package aklo;


public final class Module extends Fn {
  public final String[] names;

  public Module(Loc loc, String[] names) {
    super(loc, names[names.length - 1]);
    this.names = names;
  }
}
