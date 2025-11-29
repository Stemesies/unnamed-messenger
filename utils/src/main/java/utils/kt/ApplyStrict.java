package utils.kt;

/**
 * Позволяет совершить над объектом какое-то действие.
 * Гарантирует, что конечным результатом окажется исходный объект.
 * <br>
 * <br>Полезно для лямбда-функций, где нужно немного ограничить
 * волю, чтобы не было совершено глупых действий.
 */
@FunctionalInterface
public interface ApplyStrict<T> {

    @SuppressWarnings("UnusedReturnValue")
    T run(T it);

}
