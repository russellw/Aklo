import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public abstract class List {
  public static List of(Object... s) {
    return new BasicList(s);
  }

  public static List ofArrayList(ArrayList<Object> s) {
    return new BasicList(s.toArray());
  }

  public abstract Object[] toArray();

  public abstract Object subscript(int i);

  public abstract int len();

  public boolean isEmpty() {
    return len() == 0;
  }

  public List uniq() {
    var r = new LinkedHashSet<>(Arrays.asList(toArray()));
    return of(r.toArray());
  }

  public List slice(int i, int j) {
    i = Math.max(i, 0);
    j = Math.min(j, len());
    if (i > j) return List.of();
    var r = new Object[j - i];
    System.arraycopy(toArray(), i, r, 0, r.length);
    return of(r);
  }

  public List append(Object a) {
    var n = len();
    var r = new Object[n + 1];
    System.arraycopy(toArray(), 0, r, 0, n);
    r[n] = a;
    return of(r);
  }

  public List cat(Object s0) {
    var s = (List) s0;
    var n = len();
    var n1 = s.len();
    var r = new Object[n + n1];
    System.arraycopy(toArray(), 0, r, 0, n);
    System.arraycopy(s.toArray(), 0, r, n, n1);
    return of(r);
  }
}
