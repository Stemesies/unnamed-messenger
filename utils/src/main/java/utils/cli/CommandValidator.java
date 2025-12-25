package utils.cli;

import static utils.cli.CommandErrors.EMPTY_COMMAND;
import static utils.cli.CommandErrors.INVALID_SEPARATOR;
import static utils.cli.CommandErrors.NO_SEPARATION;
import static utils.cli.CommandErrors.UNCLOSED_QUOTE;
import static utils.cli.CommandErrors.UNEXPECTED_SYMBOL;

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
     * исключения {@link CommandError}. В него записывается что именно пошло не так
     * (Получить информацию можно через {@link CommandError#explain()}).
     * Данное сообщение пригодно для показа пользователю.
     *
     * @param command Команда для проверки
     * @return {@link CommandError}, если команда не прошла проверку
     *     <br><code>null</code> иначе.
     */
    public static CommandError validate(String command) {
        if (command.isEmpty())
            return new CommandError(EMPTY_COMMAND, command, 0, 0);

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
                    return new CommandError(NO_SEPARATION,
                        command, it.start(2) - 1, it.end(2) + 1);
                }

            if (delimiter.charAt(0) == '"') {

                if (command.charAt(it.start(2) - 1) == '\"')
                    return new CommandError(
                        NO_SEPARATION,
                        command,
                        it.start(2),
                        it.end(2)
                    );

                return new CommandError(
                    UNCLOSED_QUOTE,
                    command,
                    it.start(2),
                    it.end(2)
                );

            }

            if (delimiter.charAt(0) != ' ')
                return new CommandError(
                    INVALID_SEPARATOR,
                    command,
                    it.start(2),
                    it.end(2)
                );

            if (delimiter.length() > 1)
                return new CommandError(
                    UNEXPECTED_SYMBOL,
                    command,
                    it.start(2) + 1,
                    it.end(2)
                );
        }
        return null;
    }
}
