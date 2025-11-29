package cli;

import utils.kt.Apply;
import utils.kt.Check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static cli.CommandResults.CUSTOM_ERROR;
import static cli.CommandResults.FURTHER_SUBCOMMANDS_EXPECTED;
import static cli.CommandResults.INVALID_SUBCOMMAND;
import static cli.CommandResults.INVALID_TOKEN;
import static cli.CommandResults.MISSING_REQUIRED_ARGUMENT;
import static cli.CommandResults.UNKNOWN_SUBCOMMAND;

public class Command {

    final String base;
    private final List<Command> subcommands;
    private final List<Argument> arguments;

    final Apply<Context> action;
    final List<Condition> conditions;

    private Command(
        String base,
        List<Command> subcommands,
        List<Argument> arguments,
        Apply<Context> action,
        List<Condition> conditions
    ) {
        this.base = base;
        this.subcommands = subcommands;
        this.arguments = arguments;
        this.action = action;
        this.conditions = conditions;
    }

    public boolean is(Token token) {
        return base.equals(token.content());
    }

    public static Builder create(String baseCommand) {
        return new Builder(baseCommand);
    }

    /**
     * "Поглощает" все заданные аргументы, пока не дойдет до установленного лимита.
     * Все поглощенные аргументы заносятся в контекстный словарь.
     *
     * @param limit позиция, до которой следует поглощать аргументы.
     *              В основном это начало следующей суб-команды, либо конец строки.
     *              <br><b>[Включительно.]</b>
     *
     * @return CommandResult, если был пропущен необходимый для заполнения аргумент.
     *
     * @see Command.Builder#requireArgument(String)
     * @see Command.Builder#findArgument(String)
     */
    private CommandResult consumeArguments(Context context, int limit) {
        context.position++; // Переходим на позицию первого аргумента

        for (var argument : arguments) {

            if (context.position < limit) {
                context.arguments.put(argument.name, context.consumeToken());
                continue;
            }

            // Мы дошли до следующей суб-команды (ака до лимита)

            // Если пропущен опциональный аргумент - игнорируем и идем дальше
            if (argument.isOptional)
                return null;

            return new CommandResult(MISSING_REQUIRED_ARGUMENT,
                context.command,
                context.getToken(context.position - 1).end(),
                context.currentToken() == null
                    ? context.command.length() + 10
                    : context.currentToken().end(),
                argument.name
            );

        }
        return null;
    }

    public CommandResult execute(Context context) {
        var token = context.currentToken();
        if (token == null)
            throw new NullPointerException("Null token. Position %d. Available [0;%d)."
                .formatted(context.tokens.size(), context.position)
            );

        if (!token.is(base))
            return new CommandResult(INVALID_TOKEN, context);

        for (Condition condition : conditions) {
            if (!condition.checker.check())
                return new CommandResult(null, CUSTOM_ERROR, condition.message);
        }

        // Ищем позицию, на которой располагается следующая суб-команда:
        var nextSubcommand = context.position + 1;
        Command foundSubcommand = null;

        out: while (context.tokens.size() > nextSubcommand) {
            for (var subcommand : subcommands) {
                var sbToken = context.getToken(nextSubcommand);
                if (sbToken.is(subcommand.base) && sbToken.isFunctional()) {
                    foundSubcommand = subcommand;
                    break out;
                }
            }
            nextSubcommand++;
        }


        // Смотрим и забираем все аргументы, следующие перед найденной суб-командой
        var argConsumptionResult = consumeArguments(context, nextSubcommand);
        if (argConsumptionResult != null)
            return argConsumptionResult;

        // Нет смысла продолжать обрабатывать текущую команду; Переходим к следующей.
        if (foundSubcommand != null)
            return foundSubcommand.execute(context);

        if (action != null) {

            if (context.currentToken() != null)
                return new CommandResult(UNKNOWN_SUBCOMMAND, context);

            action.run(context);
            return null;
        }

        var lastToken = context.currentToken();
        if (lastToken != null)
            return new CommandResult(INVALID_SUBCOMMAND, context);

        var end = context.command.length();
        return new CommandResult(FURTHER_SUBCOMMANDS_EXPECTED, context.command, end, end + 10);

    }

    // -------

    // TODO: метнуться на классы пользователя и группы, когда они будут закончены.
    public static class Context {
        String command;
        String user;
        String group;

        List<Token> tokens;
        int position = 0;
        HashMap<String, Token> arguments = new HashMap<>();

        public Context(List<Token> tokens, String command, String user, String group) {
            this.tokens = tokens;
            this.command = command;
            this.user = user;
            this.group = group;
        }

        Token getToken(int index) {
            if (position > tokens.size())
                return null;
            return tokens.get(index);
        }

        Token currentToken() {
            if (position >= tokens.size())
                return null;
            return tokens.get(position);
        }

        Token consumeToken() {
            return getToken(position++);
        }

        public boolean hasArgument(String argumentName) {
            return arguments.containsKey(argumentName);
        }

        public String getString(String argumentName) throws NoSuchElementException {
            var argument = arguments.get(argumentName);
            if (argument == null)
                throw new NoSuchElementException(
                    "No arguments with name \"" + argumentName + "\" found."
                );

            return argument.content();
        }

    }

    public static class Condition {
        private final String message;
        private final Check checker;

        Condition(String message, Check checker) {
            this.message = message;
            this.checker = checker;
        }
    }

    public static class Argument {
        String name;
        boolean isOptional;

        public Argument(String name, boolean isOptional) {
            this.name = name;
            this.isOptional = isOptional;
        }

    }

    public static class Builder {

        String baseCommand;
        ArrayList<Builder> subcommands = new ArrayList<>();
        ArrayList<Argument> arguments = new ArrayList<>();
        Apply<Context> action = null;
        ArrayList<Condition> conditions = new ArrayList<>();

        public Builder(String baseCommand) {
            this.baseCommand = baseCommand;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param onFailure сообщение, выводимое при отсутствии необходимых условий.
         * @param condition условие прохода
         */
        public Builder require(String onFailure, Check condition) {
            this.conditions.add(new Condition(onFailure, condition));
            return this;
        }

        /**
         * Позволяет захватить следующий токен команды как аргумент.
         * <br>Если токен не был найден - будет выведена ошибка при выполнении команды.
         * <br>
         * <br>Пример:
         * <pre><code>
         * proc.registerCommand("groups", (it)->it
         *     .subcommand("invite" (it1)->it1
         *         .requireArgument(username)
         *         .executes(()->{})
         *      )
         * );
         * </code></pre>
         * <pre><code>
         * > /groups invite Flory
         * (Выполнение успешно)
         * </code></pre>
         * <pre><code>
         * > /groups invite
         * Missing required argument <username>."
         * /groups invite________
         *               ^^^^^^^^
         * </code></pre>
         *
         * @param name название аргумента. Будет писаться при генерации help экземпляра команды.
         *             Также необходимо для доступа к аргументу.
         */
        public Builder requireArgument(String name) {
            if (!arguments.isEmpty() && arguments.getLast().isOptional)
                throw new IllegalStateException("Non-isOptional argument after isOptional.");

            arguments.add(new Argument(name, false));
            return this;
        }

        public Builder findArgument(String name) {
            arguments.add(new Argument(name, true));
            return this;
        }

        public Builder subcommand(Builder builder) {
            subcommands.add(builder);
            return this;
        }

        public Builder subcommand(String subcommand, Apply<Builder> subcommandSettings)
            throws IllegalArgumentException {

            for (Command.Builder sb : subcommands) {
                if (subcommand.equals(sb.baseCommand))
                    throw new IllegalArgumentException(
                        "Subcommand '" + subcommand + "' already exists."
                    );
            }

            var sub = new Builder(subcommand);
            subcommands.add(sub);
            subcommandSettings.run(sub);
            return this;
        }

        /**
         * Устанавливает действие, которое будет воспроизводиться
         * при успешном выполнении команды.
         *
         * @param action проверки и действия
         */
        public Builder executes(Runnable action) {
            this.action = (it) -> action.run();
            return this;
        }

        /**
         * Устанавливает действие, которое будет воспроизводиться
         * при успешном выполнении команды.
         *
         * @param action проверки и действия
         */
        public Builder executes(Apply<Context> action) {
            this.action = action;
            return this;
        }

        public Command build() {
            if (subcommands.isEmpty() && action == null)
                throw new IllegalStateException(
                    "No subcommand or actions specified for command " + baseCommand + "."
                );

            return new Command(
                baseCommand,
                subcommands.stream()
                    .map(Builder::build)
                    .toList(),
                arguments,
                action,
                conditions
            );
        }
    }
}
