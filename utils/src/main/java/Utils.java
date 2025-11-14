public class Utils {

    /**
     * Оборачивает сообщение в формат<br>.
     * {@code [name] message}
     */
    public static String createChatMessage(Object name, Object message) {
        return "[" + name + "] " + message;
    }
}
