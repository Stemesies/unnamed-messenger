package utils.cli;

import utils.cli.utils.Argument;
import utils.cli.utils.Token;
import utils.Ansi;
import utils.StringPrintWriter;
import utils.kt.ApplyStrict;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static utils.cli.CommandErrors.COMMAND_NOT_FOUND;
import static utils.cli.CommandErrors.NOT_A_COMMAND;

/**
 * Данный класс позволяет передавать в рантайм команды определенные данные.
 * <br> Эти данные можно получать, обращаясь к контексту выполнения команды. ({@link Context#data})
 * <br> Например:
 * <pre><code>
 *     class MyData {
 *         int myField = 1;
 *     }
 *
 *     var processor = new CustomCommandProcessor<MyData>()
 *     processor.register("myCommand", (a)->a
 *         .require("Error: not 1.", (context)-> context.data.myField == 1)
 *         .executes((context)->
 *             System.out.println(
 *                 "MyData.myField == " + context.data.myField
 *             )
 *         )
 *     );
 *     processor.execute("/myCommand", new MyData());
 * </code></pre>
 * <pre><code>
 *     var myData = new MyData();
 *     processor.execute("/myCommand", myData);
 *     > MyData.myField == 1
 *     myData.myField = 2;
 *     processor.execute("/myCommand", myData);
 *     > Error: not 1.
 *
 * </code></pre>
 *
 * @see CommandProcessor
 */
public class CustomCommandProcessor<T> {

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
        return output.toString();
    }

    public StringPrintWriter getRawOutput() {
        return output;
    }

    // ---------------------------------

    @Deprecated(forRemoval = true)
    public void register(Command<T> command) {
        registeredCommands.add(command);
    }

    public void register(String command, ApplyStrict<Command.Builder<T>> action)
        throws IllegalArgumentException {

        for (Command<T> c : registeredCommands) {
            if (command.equals(c.base))
                throw new IllegalArgumentException(
                    "Command '" + command + "' already exists."
                );
        }

        Command.Builder<T> c;
        try {
            c = Command.create(command);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }

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
        lastError = internalExecute(input, contextData);
        return lastError;
    }

    private CommandError internalExecute(String input, T contextData) {
        output.clear();

        if (input.charAt(0) != '/')
            return new CommandError(NOT_A_COMMAND, input, 0, input.length());

        if (input.equals("/"))
            return new CommandError(NOT_A_COMMAND, input, 0, input.length());

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

            var context = new Context<>(output, tokens, input, contextData);
            if (command.isInvisible) {
                return command.execute(context) != null
                        ? new CommandError(COMMAND_NOT_FOUND, input, firstToken)
                        : null;
            }

            return command.execute(context);
        }

        return new CommandError(COMMAND_NOT_FOUND, input, firstToken);
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
        var path = new ArrayList<Command<T>>();
        Command<T> head = null;

        for (String subcommand : subcommands) {
            if (head == null) {
                for (var command : registeredCommands) {
                    if (!command.base.equals(subcommand)) {
                        continue;
                    }
                    head = command;
                    break;
                }
            } else {
                var hd = head;
                head = null;
                for (var command : hd.subcommands) {
                    if (!command.base.equals(subcommand)) {
                        continue;
                    }
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

        if (head == null) {
            throw new IllegalArgumentException("?");
        }

        if (head.action != null || head.isPhantom != null) {
            out.print('/');
            for (int i = 0; i < path.size() - 1; i++) {
                out.print(path.get(i).base + " ");
            }
            printCommand(out, head);
        }
        for (var cmd : head.subcommands) {
            out.print('/');
            for (Command<T> command : path) {
                out.print(command.base + " ");
            }
            printCommand(out, cmd);
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
