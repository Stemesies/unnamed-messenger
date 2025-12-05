package cli;

import cli.utils.Argument;
import cli.utils.Condition;
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
import static cli.CommandResults.PHANTOM_COMMAND;
import static cli.CommandResults.UNKNOWN_SUBCOMMAND;

public class Command<T /*extends ContextData*/> {

    final String base;
    final String helpDescription;
    final List<Command<T>> subcommands;
    final List<Argument> arguments;

    final Apply<Context<T>> action;
    final List<Condition<T>> conditions;

    final CommandError isPhantom;

    private Command(
        String base,
        String helpDescription,
        List<Command<T>> subcommands,
        List<Argument> arguments,
        Apply<Context<T>> action,
        List<Condition<T>> conditions,
        CommandError isPhantom
    ) {
        this.base = base;
        this.helpDescription = helpDescription;
        this.subcommands = subcommands;
        this.arguments = arguments;
        this.action = action;
        this.conditions = conditions;
        this.isPhantom = isPhantom;
    }

    public boolean is(Token token) {
        return base.equals(token.content());
    }

    public static <T /*extends ContextData*/> Builder<T> create(String baseCommand) {
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
    private CommandError consumeArguments(Context<T> context, int limit) {
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

            return new CommandError(MISSING_REQUIRED_ARGUMENT,
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

    public CommandError execute(Context<T> context) {
        var token = context.currentToken();
        if (token == null)
            throw new NullPointerException("Null token. Position %d. Available [0;%d)."
                .formatted(context.tokens.size(), context.position)
            );

        if (!token.is(base))
            return new CommandError(INVALID_TOKEN, context);

        for (Condition<T> condition : conditions) {
            if (!condition.checker.check(context))
                return new CommandError(null, CUSTOM_ERROR, condition.message);
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
                return new CommandError(UNKNOWN_SUBCOMMAND, context);

            action.run(context);
            return null;
        }

        var lastToken = context.currentToken();
        if (lastToken != null)
            return new CommandError(INVALID_SUBCOMMAND, context);

        var end = context.command.length();
        return new CommandError(FURTHER_SUBCOMMANDS_EXPECTED, context.command, end, end + 10);

    }

    // -------

    public static class Builder<T /*extends ContextData*/> {

        String baseCommand;
        String helpDescription = null;
        ArrayList<Builder<T>> subcommands = new ArrayList<>();
        ArrayList<Argument> arguments = new ArrayList<>();
        Apply<Context<T>> action = null;
        ArrayList<Condition<T>> conditions = new ArrayList<>();
        CommandError isPhantom = null;
        boolean isBase = true;

        public Builder(String baseCommand) {
            this.baseCommand = baseCommand;
        }

        /**
         * Позволяет заявить команде /help, что данная команда существует.
         * При этом ее нельзя будет вызвать:
         * процессор будет выдавать ошибку {@link CommandResults#COMMAND_NOT_FOUND}.
         * <br>
         * <br> !! Данный метод не должен применяться к суб-командам !!
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder<T> isPhantom(String msg) throws IllegalStateException {
            if (!isBase)
                throw new IllegalStateException(
                    "isPhantom flag should not be set for subcommands."
                );
            isPhantom = new CommandError(null, PHANTOM_COMMAND, msg);
            return this;
        }

        /**
         * Позволяет заявить команде /help, что данная команда существует.
         * При этом ее нельзя будет вызвать:
         * процессор будет выдавать ошибку {@link CommandResults#PHANTOM_COMMAND}.
         * <br>
         * <br> !! Данный метод не должен применяться к суб-командам !!
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder<T> isPhantom() throws IllegalStateException {
            return isPhantom("Command is unavailable.");
        }

        public Builder<T> description(String helpDescription) {
            this.helpDescription = helpDescription;
            return this;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param condition условие прохода
         * @throws IllegalStateException если команда фантомная.
         */
        public Builder<T> require(Condition<T> condition) throws IllegalStateException {
            if (isPhantom != null)
                throw new IllegalStateException(
                    "Conditions should not be used for phantom commands."
                );
            this.conditions.add(condition);
            return this;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param onFailure сообщение, выводимое при отсутствии необходимых условий.
         * @param condition условие прохода
         * @throws IllegalStateException если команда фантомная.
         */
        public Builder<T> require(String onFailure, Check condition) throws IllegalStateException {
            return require(new Condition<>(onFailure, condition));
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param onFailure сообщение, выводимое при отсутствии необходимых условий.
         * @param condition условие прохода
         */
        public Builder<T> require(String onFailure, CheckIf<Context<T>> condition) {
            return require(new Condition<>(onFailure, condition));
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

        public Builder<T> subcommand(String subcommand, Apply<Builder<T>> subcommandSettings)
            throws IllegalArgumentException {

            for (Command.Builder<T> sb : subcommands) {
                if (subcommand.equals(sb.baseCommand))
                    throw new IllegalArgumentException(
                        "Subcommand '" + subcommand + "' already exists."
                    );
            }

            var sub = new Builder<T>(subcommand);

            sub.isPhantom = isPhantom;
            sub.isBase = false;
            subcommands.add(sub);
            subcommandSettings.run(sub);
            return this;
        }

        /**
         * Устанавливает действие, которое будет воспроизводиться
         * при успешном выполнении команды.
         *
         * @param action Проверки и действия
         * @throws IllegalStateException Если команда фантомная
         */
        public Builder<T> executes(Runnable action) throws IllegalStateException {
            if (isPhantom != null)
                throw new IllegalStateException(
                    "Actions should not be used for phantom commands."
                );
            this.action = (it) -> action.run();
            return this;
        }

        /**
         * Устанавливает действие, которое будет воспроизводиться
         * при успешном выполнении команды.
         *
         * @param action проверки и действия
         * @throws IllegalStateException Если команда фантомная
         */
        public Builder<T> executes(Apply<Context<T>> action) throws IllegalStateException {
            if (isPhantom != null)
                throw new IllegalStateException(
                    "Actions should not be used for phantom commands."
                );
            this.action = action;
            return this;
        }

        public Command<T> build() {
            if (subcommands.isEmpty() && action == null && isPhantom == null)
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
                conditions,
                isPhantom
            );
        }
    }
}
