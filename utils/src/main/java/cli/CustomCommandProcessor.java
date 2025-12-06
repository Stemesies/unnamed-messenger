package cli;

import cli.utils.Argument;
import cli.utils.Token;
import utils.Ansi;
import utils.StringPrintWriter;
import utils.kt.ApplyStrict;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cli.CommandErrors.COMMAND_NOT_FOUND;
import static cli.CommandErrors.NOT_A_COMMAND;

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

    // ---------------------------------

    @Deprecated(forRemoval = true)
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

            return command.execute(new Context<>(output, tokens, input, contextData));
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
            out.print('/' + cmd.base);

            for (Argument arg : cmd.arguments)
                out.print(" " + arg);
            out.println();
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
