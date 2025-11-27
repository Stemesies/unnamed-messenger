package cli;

import static cli.CommandResults.EMPTY_COMMAND;
import static cli.CommandResults.INVALID_SEPARATOR;
import static cli.CommandResults.NO_SEPARATION;
import static cli.CommandResults.UNCLOSED_QUOTE;
import static cli.CommandResults.UNEXPECTED_SYMBOL;

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
     * исключения {@link CommandResult}. В него записывается что именно пошло не так
     * (Получить информацию можно через {@link CommandResult#explain()}).
     * Данное сообщение пригодно для показа пользователю.
     *
     * @param command Команда для проверки
     * @return {@link CommandResult}, если команда не прошла проверку
     *     <br><code>null</code> иначе.
     */
    public static CommandResult validate(String command) {
        if (command.isEmpty())
            return new CommandResult(EMPTY_COMMAND, command, 0, 0);

        var parserOutput = CommandProcessor.pattern
            .matcher(command)
            .results()
            .toList();

        for (int i = 0; i < parserOutput.size(); i++) {
            var it = parserOutput.get(i);
            String delimiter = it.group(2);

            if (delimiter == null)
                continue;

            if (delimiter.isEmpty())
                if (i == parserOutput.size() - 1)
                    return null;
                else {
                    return new CommandResult(NO_SEPARATION,
                        command, it.start(2) - 1, it.end(2) + 1);
                }

            if (delimiter.charAt(0) == '"') {

                if (command.charAt(it.start(2) - 1) == '\"')
                    return new CommandResult(
                        NO_SEPARATION,
                        command,
                        it.start(2),
                        it.end(2)
                    );

                return new CommandResult(
                    UNCLOSED_QUOTE,
                    command,
                    it.start(2),
                    it.end(2)
                );

            }

            if (delimiter.charAt(0) != ' ')
                return new CommandResult(
                    INVALID_SEPARATOR,
                    command,
                    it.start(2),
                    it.end(2)
                );

            if (delimiter.length() > 1)
                return new CommandResult(
                    UNEXPECTED_SYMBOL,
                    command,
                    it.start(2) + 1,
                    it.end(2)
                );
        }
        return null;
    }
}
