package utils.kt;

/**
 * Позволяет совершить над объектом какое-то действие.
 * <br>Полезно для лямбда-функций.
 */
@FunctionalInterface
public interface  Apply<T> {
    void run(T it);
}
