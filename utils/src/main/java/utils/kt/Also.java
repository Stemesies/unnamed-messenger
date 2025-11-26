package utils.kt;

/**
 * Позволяет совершить над объектом какое-то действие,
 * не прерывая цепочку действий.
 * <br>Полезно для лямбда-функций.
 */
@FunctionalInterface
public interface Also<T> {

    @SuppressWarnings("UnusedReturnValue")
    T run(T it);

}
