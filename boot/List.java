import java.util.ArrayList;

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

  public List slice(int i, int j) {
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
