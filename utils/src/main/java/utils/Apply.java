package utils;

@FunctionalInterface
public interface  Apply<T> {
    void run(T it);
}
