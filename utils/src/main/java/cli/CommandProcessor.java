package cli;

import utils.Ansi;
import utils.kt.ApplyStrict;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cli.CommandResults.COMMAND_NOT_FOUND;
import static cli.CommandResults.NOT_A_COMMAND;

public class CommandProcessor {

    private final StringPrintWriter output = new StringPrintWriter();
    private CommandResult lastError = null;
    private final ArrayList<Command> registeredCommands = new ArrayList<>();

    static final Pattern pattern = Pattern.compile(
        "^/" // Начало строки
            + "|(\\w+|(?<!\")\"\"|(?<!\")\".*?(?:(?<=[^\\\\])(?:\\\\\\\\)+|[^\\\\])\")"
            + "|([^\"^[:alnum]]+?(?=\\w|\"|$)|\".*)" // Сепараторы
    );

    public CommandProcessor() {
        createHelpCommand();
    }

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

    public String getOutput() {
        return output.toString();
    }

    // ---------------------------------

    public void register(Command command) {
        registeredCommands.add(command);
    }

    public void register(String command, ApplyStrict<Command.Builder> action)
        throws IllegalStateException, IllegalArgumentException {

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
        System.out.print(getOutput());

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
        output.clear();

        if (input.charAt(0) != '/')
            return new CommandResult(NOT_A_COMMAND, input, 0, input.length());
        if (input.equals("/"))
            return new CommandResult(NOT_A_COMMAND, input, 0, input.length());

        var validatorError = CommandValidator.validate(input);
        if (validatorError != null)
            return validatorError;

        List<Token> tokens = CommandTokenizer.tokenize(input);
        var firstToken = tokens.getFirst();

        for (var command : registeredCommands) {
            if (!command.is(firstToken))
                continue;

            if (command.isPhantom != null)
                return command.isPhantom;

            var context = new Command.Context(output, tokens, input, null, null);
            if (command.isInvisible) {
                return command.execute(context) != null
                    ? new CommandResult(COMMAND_NOT_FOUND, input, firstToken)
                    : null;
            }
            return command.execute(context);
        }

        return new CommandResult(COMMAND_NOT_FOUND, input, firstToken);
    }

    private void createHelpCommand() {
        register("help", it1 -> it1
            .description("выводит все доступные команды")
            .requireArrayArgument("subcommands")
            .executes((ctx) -> {
                var list = ctx.getArray("subcommands");
                if (!list.isEmpty())
                    printAllPossibleCommands(ctx.out, list);
                else
                    printAllPossibleCommands(ctx.out);
            })
        );
    }

    private void printAllPossibleCommands(StringPrintWriter out, List<String> subcommands) {
        var path = new ArrayList<Command>();
        Command head = null;

        for (String subcommand : subcommands) {
            if (head == null) {
                for (var command : registeredCommands) {
                    if (!command.base.equals(subcommand)) continue;
                    head = command;
                    break;
                }
            } else {
                var hd = head;
                head = null;
                for (var command : hd.subcommands) {
                    if (!command.base.equals(subcommand)) continue;
                    head = command;
                    break;
                }

            }
            if (head == null || head.isInvisible) {
                out.println(Ansi.applyStyle(
                    "Unknown command " + subcommand + ".", Ansi.Colors.RED
                ));
                return;
            }

            path.add(head);
        }
        if (head == null)
            throw new IllegalArgumentException("?");

        if (head.action != null || head.isPhantom != null) {
            out.print('/');
            for (int i = 0; i < path.size() - 1; i++) {
                out.print(path.get(i).base + " ");
            }
            printCommand(out, head);
        }
        for (var scmd : head.subcommands) {
            out.print('/');
            for (Command command : path) {
                out.print(command.base + " ");
            }

            printCommand(out, scmd);
        }
    }

    private void printAllPossibleCommands(StringPrintWriter out) {
        registeredCommands.forEach(cmd -> {
            if (!cmd.isInvisible) {
                out.print('/');
                printCommand(out, cmd);
            }
        });
    }

    private void printCommand(StringPrintWriter out, Command cmd) {
        out.print(cmd.base);

        for (Command.Argument arg : cmd.arguments)
            out.print(" " + arg);

        if (cmd.helpDescription != null) {
            out.print(" - " + cmd.helpDescription);
        }
        out.println();
    }
}
