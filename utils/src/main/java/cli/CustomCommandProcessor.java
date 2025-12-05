package cli;

import cli.utils.Argument;
import cli.utils.Token;
import utils.Ansi;
import utils.StringPrintWriter;
import utils.kt.ApplyStrict;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cli.CommandResults.COMMAND_NOT_FOUND;
import static cli.CommandResults.NOT_A_COMMAND;

public class CustomCommandProcessor<T/* extends ContextData*/> {

    private final StringPrintWriter output = new StringPrintWriter();
    private CommandError lastError = null;
    private final ArrayList<Command<T>> registeredCommands = new ArrayList<>();

    static final Pattern pattern = Pattern.compile(
        "^/" // Начало строки
            + "|(\\w+|(?<!\")\"\"|(?<!\")\".*?(?:(?<=[^\\\\])(?:\\\\\\\\)+|[^\\\\])\")"
            + "|([^\"^[:alnum]]+?(?=\\w|\"|$)|\".*)" // Сепараторы
    );

    public CustomCommandProcessor() {
        createHelpCommand();
    }

    /**
     * Возвращает ошибку при прошлой обработке команды. Значение обновляется по завершении
     * исполнения команды, во время возврата функции
     * {@link #execute(String, T)}.
     *
     * @return <code>IllegalCommandResult</code>, если предыдущий вызов
     *     {@link #execute(String, T)} завершился неудачей
     *     <br><code>null</code>, если все прошло успешно
     */
    public CommandError getLastError() {
        return lastError;
    }

    public String getOutput() {
        return output.isEmpty() ? null : output.toString();
    }

    // ---------------------------------

    public void register(Command<T> command) {
        registeredCommands.add(command);
    }

    public void register(String command, ApplyStrict<Command.Builder<T>> action)
        throws IllegalStateException {

        var c = Command.<T>create(command);
        action.run(c);
        registeredCommands.add(c.build());
    }

    /**
     * Исполняет команду. При неудаче выводит сообщение об ошибке.
     * <br>При ошибке вы можете получить информацию с помощью
     * {@link CustomCommandProcessor#getLastError()}.
     *
     * @return true, если команда была успешно выполнена.
     *     <br>false, если возникла ошибка
     */
    public boolean executeAndExplain(String input, T contextData) {
        var result = execute(input, contextData);

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
    public CommandError execute(String input, T contextData) {
        output.clear();

        if (input.charAt(0) != '/') {
            lastError = new CommandError(NOT_A_COMMAND, input, 0, input.length());
            return lastError;
        }

        if (input.equals("/")) {
            lastError = new CommandError(NOT_A_COMMAND, input, 0, input.length());
            return lastError;
        }

        lastError = CommandValidator.validate(input);
        if (lastError != null)
            return lastError;

        List<Token> tokens = CommandTokenizer.tokenize(input);
        var firstToken = tokens.getFirst();

        for (var command : registeredCommands) {
            if (!command.is(firstToken))
                continue;

            if (command.isPhantom != null)
                return command.isPhantom;
            lastError = command.execute(new Context<>(output, tokens, input, contextData));
            return lastError;
        }

        return new CommandError(COMMAND_NOT_FOUND, input, firstToken);
    }

    private void createHelpCommand() {
        register("help", it1 -> it1
            .description("выводит все доступные команды")
            .findArgument("subcommand")
            .executes((ctx) -> {
                if (ctx.hasArgument("subcommand"))
                    printAllPossibleCommands(ctx.out, ctx.getString("subcommand"));
                else
                    printAllPossibleCommands(ctx.out);
            })
        );
    }

    private void printAllPossibleCommands(StringPrintWriter out, String subcommand) {
        Command<T> cmd = null;
        for (var command : registeredCommands)
            if (command.base.equals(subcommand)) {
                cmd = command;
                break;
            }
        if (cmd == null) {
            out.println(Ansi.applyStyle("Unknown command.", Ansi.Colors.RED));
            return;
        }

        if (cmd.action != null || cmd.isPhantom != null) {
            out.print('/');
            printCommand(out, cmd);
        }
        for (var scmd : cmd.subcommands) {
            out.print("/" + cmd.base + " ");
            printCommand(out, scmd);
        }
    }

    private void printAllPossibleCommands(StringPrintWriter out) {
        registeredCommands.forEach(cmd -> {
            out.print('/');
            printCommand(out, cmd);
        });
    }

    private void printCommand(StringPrintWriter out, Command<T> cmd) {
        out.print(cmd.base);

        for (Argument arg : cmd.arguments)
            out.print(" " + arg);

        if (cmd.helpDescription != null) {
            out.print(" - " + cmd.helpDescription);
        }
        out.println();
    }
}
