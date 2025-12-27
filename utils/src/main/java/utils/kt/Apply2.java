package utils.kt;

/**
 * Позволяет совершить над объектом какое-то действие.
 * <br>Полезно для лямбда-функций.
 */
@FunctionalInterface
public interface Apply2<T, R> {
    void run(T it1, R it2);
}
