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
     * исключения {@link IllegalCommandResult}. В него записывается что именно пошло не так
     * (Получить информацию можно через {@link IllegalCommandResult#explain()}).
     * Данное сообщение пригодно для показа пользователю.
     *
     * @param command Команда для проверки
     * @return {@link IllegalCommandResult}, если команда не прошла проверку
     *     <br><code>null</code> иначе.
     */
    public static IllegalCommandResult validate(String command) {
        if (command.isEmpty())
            return new IllegalCommandResult("Command is empty.", command, 0, 0);

        var parserOutput = CommandProcessor.pattern
            .matcher(command)
            .results()
            .toList();

        for (int i = 0; i < parserOutput.size(); i++) {
            var it = parserOutput.get(i);

            String delimiter = it.group(2);
            if (delimiter == null)
                continue;

            if (delimiter.isEmpty()) {
                if (i == parserOutput.size() - 1)
                    return null;
                else {
                    return new IllegalCommandResult("No separation found:",
                        command, it.start(2) - 1, it.end(2) + 1);
                }
            }

            if (delimiter.charAt(0) == '"') {
                return new IllegalCommandResult("Unclosed quoted argument:",
                    command, it.start(2) - 1, it.end(2));
            }

            if (delimiter.charAt(0) != ' ') {
                return new IllegalCommandResult("Invalid symbol:",
                    command, it.start(2) - 1, it.end(2));
            }

            if (delimiter.length() > 1) {
                return new IllegalCommandResult(
                    "Invalid amount of symbols and/or invalid symbols:",
                    command, it.start(2), it.end(2)
                );
            }

        }
        return null;
    }
}
