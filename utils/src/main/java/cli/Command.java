package cli;

import cli.utils.Argument;
import cli.utils.Condition;
import cli.utils.ContextData;
import cli.utils.Token;
import utils.kt.Apply;
import utils.kt.Check;
import utils.kt.CheckIf;

import java.util.ArrayList;
import java.util.List;

import static cli.CommandResults.CUSTOM_ERROR;
import static cli.CommandResults.FURTHER_SUBCOMMANDS_EXPECTED;
import static cli.CommandResults.INVALID_SUBCOMMAND;
import static cli.CommandResults.INVALID_TOKEN;
import static cli.CommandResults.MISSING_REQUIRED_ARGUMENT;
import static cli.CommandResults.UNKNOWN_SUBCOMMAND;

public class Command<T extends ContextData> {

    final String base;
    final String helpDescription;
    final List<Command<T>> subcommands;
    final List<Argument> arguments;

    final Apply<Context<T>> action;
    final List<Condition<T>> conditions;

    private Command(
        String base,
        String helpDescription,
        List<Command<T>> subcommands,
        List<Argument> arguments,
        Apply<Context<T>> action,
        List<Condition<T>> conditions
    ) {
        this.base = base;
        this.helpDescription = helpDescription;
        this.subcommands = subcommands;
        this.arguments = arguments;
        this.action = action;
        this.conditions = conditions;
    }

    public boolean is(Token token) {
        return base.equals(token.content());
    }

    public static <T extends ContextData> Builder<T> create(String baseCommand) {
        return new Builder<>(baseCommand);
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
    private CommandResult consumeArguments(Context<T> context, int limit) {
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

    public CommandResult execute(Context<T> context) {
        var token = context.currentToken();
        if (token == null)
            throw new NullPointerException("Null token. Position %d. Available [0;%d)."
                .formatted(context.tokens.size(), context.position)
            );

        if (!token.is(base))
            return new CommandResult(INVALID_TOKEN, context);

        for (Condition<T> condition : conditions) {
            if (!condition.checker.check(context))
                return new CommandResult(null, CUSTOM_ERROR, condition.message);
        }

        // Ищем позицию, на которой располагается следующая суб-команда:
        var nextSubcommand = context.position + 1;
        Command<T> foundSubcommand = null;

        out:
        while (context.tokens.size() > nextSubcommand) {
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

    public static class Builder<T extends ContextData> {

        String baseCommand;
        String helpDescription = null;
        ArrayList<Builder<T>> subcommands = new ArrayList<>();
        ArrayList<Argument> arguments = new ArrayList<>();
        Apply<Context<T>> action = null;
        ArrayList<Condition<T>> conditions = new ArrayList<>();

        public Builder(String baseCommand) {
            this.baseCommand = baseCommand;
        }

        public Builder<T> description(String helpDescription) {
            this.helpDescription = helpDescription;
            return this;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param onFailure сообщение, выводимое при отсутствии необходимых условий.
         * @param condition условие прохода
         */
        public Builder<T> require(String onFailure, Check condition) {
            this.conditions.add(new Condition<>(onFailure, condition));
            return this;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param onFailure сообщение, выводимое при отсутствии необходимых условий.
         * @param condition условие прохода
         */
        public Builder<T> require(String onFailure, CheckIf<Context<T>> condition) {
            this.conditions.add(new Condition<>(onFailure, condition));
            return this;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param condition условие прохода
         */
        public Builder<T> require(Condition<T> condition) {
            this.conditions.add(condition);
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
        public Builder<T> requireArgument(String name) {
            if (!arguments.isEmpty() && arguments.getLast().isOptional)
                throw new IllegalStateException("Non-isOptional argument after isOptional.");

            arguments.add(new Argument(name, false));
            return this;
        }

        public Builder<T> findArgument(String name) {
            arguments.add(new Argument(name, true));
            return this;
        }

        public Builder<T> subcommand(Builder<T> builder) {
            subcommands.add(builder);
            return this;
        }

        public Builder<T> subcommand(String subcommand, Apply<Builder<T>> subcommandSettings)
            throws IllegalArgumentException {

            for (Command.Builder<T> sb : subcommands) {
                if (subcommand.equals(sb.baseCommand))
                    throw new IllegalArgumentException(
                        "Subcommand '" + subcommand + "' already exists."
                    );
            }

            var sub = new Builder<T>(subcommand);
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
        public Builder<T> executes(Runnable action) {
            this.action = (it) -> action.run();
            return this;
        }

        /**
         * Устанавливает действие, которое будет воспроизводиться
         * при успешном выполнении команды.
         *
         * @param action проверки и действия
         */
        public Builder<T> executes(Apply<Context<T>> action) {
            this.action = action;
            return this;
        }

        public Command<T> build() {
            if (subcommands.isEmpty() && action == null)
                throw new IllegalStateException(
                    "No subcommand or actions specified for command " + baseCommand + "."
                );

            return new Command<>(
                baseCommand,
                helpDescription,
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
