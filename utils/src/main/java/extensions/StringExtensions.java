package extensions;

public class StringExtensions {

    /**
     * Отсекает определенное количество символов с начала строки.
     *
     * <br>Например, при выполнении команды drop("amogus", 2)
     * первые 2 буквы отсекутся, оставляя лишь "ogus".
     *
     * @param str Строка для отсечения.
     * @param n   Количество символов, которые нужно отсечь.
     * @return Урезанная строка.
     *
     * @throws IndexOutOfBoundsException если n больше, чем длина строки.
     */
    public static String drop(String str, int n) throws IndexOutOfBoundsException {
        return str.substring(n);
    }

    /**
     * Отсекает определенное количество символов с конца строки.
     *
     * <br>Например, при выполнении команды dropLast("amogus", 2)
     * последние 2 буквы отсекутся, оставляя лишь "amog".
     *
     * @param str Строка для отсечения.
     * @param n   Количество символов, которые нужно отсечь.
     * @return Урезанная строка.
     *
     * @throws IndexOutOfBoundsException если n больше, чем длина строки.
     */
    public static String dropLast(String str, int n) throws IndexOutOfBoundsException {
        return str.substring(0, str.length() - n);
    }
}
