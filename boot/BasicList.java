import java.util.Arrays;

public final class BasicList extends List {
  final Object[] data;

  public BasicList(Object[] data) {
    this.data = data;
  }

  @Override
  public Object[] toArray() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    // TODO move up
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasicList basicList = (BasicList) o;
    return Arrays.equals(data, basicList.data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }

  @Override
  public Object subscript(int i) {
    return data[i];
  }

  @Override
  public int len() {
    return data.length;
  }
}
