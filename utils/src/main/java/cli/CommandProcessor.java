package cli;

import utils.kt.ApplyStrict;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cli.CommandResults.COMMAND_NOT_FOUND;
import static cli.CommandResults.NOT_A_COMMAND;

public class CommandProcessor {

    private CommandResult lastError = null;
    private final ArrayList<Command> registeredCommands = new ArrayList<>();

    static final Pattern pattern = Pattern.compile(
        "^/" // Начало строки
            + "|(\\w+|(?<!\")\"\"|(?<!\")\".*?(?:(?<=[^\\\\])(?:\\\\\\\\)+|[^\\\\])\")"
            + "|([^\"^[:alnum]]+?(?=\\w|\"|$)|\".*)" // Сепараторы
    );

    /**
     * Возвращает ошибку при прошлой обработке команды. Значение обновляется по завершении
     * исполнения команды, во время возврата функции {@link #executeAndExplain(String)}.
     *
     * @return <code>IllegalCommandResult</code>, если предыдущий вызов
     *     {@link #executeAndExplain(String)} завершился неудачей
     *     <br><code>null</code>, если все прошло успешно
     */
    public CommandResult getLastError() {
        return lastError;
    }

    // ---------------------------------

    public void register(Command command) {
        registeredCommands.add(command);
    }

    public void register(String command, ApplyStrict<Command.Builder> action)
        throws IllegalStateException {

        var c = Command.create(command);
        action.run(c);
        registeredCommands.add(c.build());
    }

    /**
     * Исполняет команду. При неудаче выводит сообщение об ошибке.
     * <br>При ошибке вы можете получить информацию с помощью
     * {@link CommandProcessor#getLastError()}.
     *
     * @return true, если команда была успешно выполнена.
     *     <br>false, если возникла ошибка
     */
    public boolean executeAndExplain(String input) {
        var result = execute(input);

        if (result != null) {
            lastError = result;
            result.explain();
            return false;
        }

        return true;
    }

    /**
     * Исполняет команду.
     * <br>При ошибке вы можете получить информацию с помощью
     * {@link CommandProcessor#getLastError()}.
     *
     * @return true, если команда была успешно выполнена.
     *     <br>false, если возникла ошибка
     */
    public CommandResult execute(String input) {
        if (input.charAt(0) != '/')
            return new CommandResult(NOT_A_COMMAND, input, 0, input.length());

        var validatorError = CommandValidator.validate(input);
        if (validatorError != null)
            return validatorError;

        List<Token> tokens = CommandTokenizer.tokenize(input);
        var firstToken = tokens.getFirst();

        for (var command : registeredCommands)
            if (command.is(firstToken))
                return command.execute(new Command.Context(tokens, input, "", ""));

        return new CommandResult(COMMAND_NOT_FOUND, input, firstToken);
    }

}
