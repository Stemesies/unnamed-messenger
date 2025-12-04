package utils.kt;

@FunctionalInterface
public interface CheckIf<T> {
    boolean check(T value);
}
