package aklo;

@SuppressWarnings("unused")
@FunctionalInterface
interface TriFunction<T, U, V, R> {
  R apply(T t, U u, V v);
}
