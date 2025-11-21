package cli;

public class CommandValidator {

    /**
     * Проверяет введенную команду на отсутствие синтаксических ошибок.
     * <br>В частности отсеивает команды:
     * <ul>
     *     <li> Пустые
     *     <li> С незакрытыми мультисловными аргументами (те, что в кавычках)
     *     <li> С мультисловными аргументами, находящимися впритык
     *     <li> С неверными разделителями
     *     <li> Со слишком большими разделителями
     * </ul>
     *
     * <br>По завершении, если команда была отсеяна, программа составляет экземпляр
     * исключения {@link IllegalCommandException}. В него записывается что именно пошло не так
     * (Получить информацию можно через {@link IllegalCommandException#explain()}).
     * Данное сообщение пригодно для показа пользователю.
     *
     * @param command Команда для проверки
     * @return {@link IllegalCommandException}, если команда не прошла проверку
     *     <br><code>null</code> иначе.
     */
    public static IllegalCommandException validate(String command) {
        if (command.isEmpty())
            return new IllegalCommandException("Command is empty.", command, 0, 0);

        var parserOutput = CommandProcessor.pattern
            .matcher(command)
            .results()
            .toList();

        for (int i = 0; i < parserOutput.size(); i++) {
            var it = parserOutput.get(i);

            String delimiter = it.group(2);
            if (delimiter == null)
                continue;
            System.out.println("Delimiter: \"" + delimiter + "\"");

            if (delimiter.isEmpty()) {
                if (i == parserOutput.size() - 1)
                    return null;
                else {
                    return new IllegalCommandException("No separation found:",
                        command, it.start(2) - 1, it.end(2) + 1);
                }
            }

            if (delimiter.charAt(0) == '"') {
                return new IllegalCommandException("Unclosed quoted argument:",
                    command, it.start(2) - 1, it.end(2));
            }

            if (delimiter.charAt(0) != ' ') {
                return new IllegalCommandException("Invalid symbol:",
                    command, it.start(2) - 1, it.end(2));
            }

            if (delimiter.length() > 1) {
                return new IllegalCommandException(
                    "Invalid amount of symbols and/or invalid symbols:",
                    command, it.start(2), it.end(2)
                );
            }

        }
        return null;
    }
}
