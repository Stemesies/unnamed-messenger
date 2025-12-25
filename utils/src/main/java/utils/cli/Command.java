package utils.cli;

import utils.cli.utils.Argument;
import utils.cli.utils.Condition;
import utils.cli.utils.Token;
import utils.kt.Apply;
import utils.kt.Check;
import utils.kt.CheckIf;

import java.util.ArrayList;
import java.util.List;

import static utils.cli.CommandErrors.CUSTOM_ERROR;
import static utils.cli.CommandErrors.FURTHER_SUBCOMMANDS_EXPECTED;
import static utils.cli.CommandErrors.INVALID_SUBCOMMAND;
import static utils.cli.CommandErrors.INVALID_TOKEN;
import static utils.cli.CommandErrors.MISSING_REQUIRED_ARGUMENT;
import static utils.cli.CommandErrors.PHANTOM_COMMAND;
import static utils.cli.CommandErrors.UNKNOWN_SUBCOMMAND;

public class Command<T> {

    final String base;
    final String helpDescription;
    final List<Command<T>> subcommands;
    final List<Argument> arguments;

    final Apply<Context<T>> action;
    final List<Condition<T>> conditions;

    final CommandError isPhantom;
    final boolean isInvisible;

    private Command(
        String base,
        String helpDescription,
        List<Command<T>> subcommands,
        List<Argument> arguments,
        Apply<Context<T>> action,
        List<Condition<T>> conditions,
        CommandError isPhantom,
        boolean isInvisible
    ) {
        this.base = base;
        this.helpDescription = helpDescription;
        this.subcommands = subcommands;
        this.arguments = arguments;
        this.action = action;
        this.conditions = conditions;
        this.isPhantom = isPhantom;
        this.isInvisible = isInvisible;
    }

    public boolean is(Token token) {
        return base.equals(token.content());
    }

    public static <T> Builder<T> create(String baseCommand) throws IllegalArgumentException {
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

            if (argument.isArray) {
                var list = new ArrayList<Token>();
                while (context.position < limit) {
                    list.add(context.consumeToken());
                }
                context.arguments.put(argument.name, list.toArray());
                continue;
            } else {
                if (context.position < limit) {
                    context.arguments.put(argument.name, context.consumeToken());
                    continue;
                }
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

    public static class Builder<T> {

        String baseCommand;
        String helpDescription = null;
        ArrayList<Builder<T>> subcommands = new ArrayList<>();
        ArrayList<Argument> arguments = new ArrayList<>();
        Apply<Context<T>> action = null;
        ArrayList<Condition<T>> conditions = new ArrayList<>();
        CommandError isPhantom = null;
        boolean isInvisible = false;
        boolean isBase = true;

        public Builder(String baseCommand) throws IllegalArgumentException {
            if (baseCommand.isEmpty())
                throw new IllegalArgumentException("Empty command.");
            if (baseCommand.charAt(0) == '/')
                throw new IllegalArgumentException(
                    "Whooptie, seems like you accidentally added '/' to the beginning."
                    + "Don't do that :3"
                );

            this.baseCommand = baseCommand;
        }

        /**
         * Позволяет заявить команде /help, что данная команда существует.
         * При этом ее нельзя будет вызвать:
         * процессор будет выдавать особую ошибку {@link CommandErrors#PHANTOM_COMMAND}.
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
         * процессор будет выдавать особую ошибку {@link CommandErrors#PHANTOM_COMMAND}.
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder<T> isPhantom() throws IllegalStateException {
            return isPhantom("Command is unavailable.");
        }

        /**
         * Позволяет заявить команде /help, что данная команда отсутствует,
         * даже если она зарегистрирована.<br>
         * При этом, если команда написана неправильно, будет высвечиваться ошибка
         * {@link CommandErrors#COMMAND_NOT_FOUND}
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder<T> isInvisible() throws IllegalStateException {
            if (!isBase)
                throw new IllegalStateException(
                        "isInvisible flag should not be set for subcommands."
                );
            isInvisible = true;
            return this;
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
         * proc.registerCommand("invite", (a)->a
         *     .requireArgument("username")
         *     .executes((ctx)->{
         *         ctx.out.println(ctx.getString("username"))
         *     })
         * );
         * </code></pre>
         * <pre><code>
         * > /invite Flory
         * Flory
         * > /invite
         * Missing required argument <username>.
         * /invite________
         *        ^^^^^^^^
         * </code></pre>
         * <br>
         * Советую хорошо называть аргументы, так как <code>name</code> будет выводиться
         * при вызове /help.
         *
         * @param name название аргумента
         * @throws IllegalStateException если вызвано после запроса опционального аргумента
         *      или массива аргументов
         *
         * @see Context#getString(String) получение запрашиваемых аргументов
         */
        public Builder<T> requireArgument(String name) throws IllegalStateException {
            if (!arguments.isEmpty() && arguments.getLast().isOptional)
                throw new IllegalStateException("Non-isOptional argument after optional.");
            if (!arguments.isEmpty() && arguments.getLast().isArray)
                throw new IllegalStateException("Can't specify arguments after Array argument");

            arguments.add(new Argument(name, false));
            return this;
        }

        /**
         * Позволяет захватить следующие n токенов, пока не будет встречена суб-команда
         * или не кончится команда.
         * <br>
         * <br>Пример:
         * <pre><code>
         * proc.registerCommand("invite", (a)->a
         *     .requireArrayArgument("usernames")
         *     .executes((ctx)->{
         *         ctx.out.println(ctx.getArray("usernames"))
         *     })
         * );
         * </code></pre>
         * <pre><code>
         * > /invite
         * []
         * > /invite Flory
         * [Flory]
         * > /invite Flory Buteo Stemie
         * [Flory, Buteo, Stemie]
         * </code></pre>
         * <br>
         * Советую хорошо называть аргументы, так как <code>name</code> будет выводиться
         * при вызове /help.
         *
         * @param name название массива аргументов
         * @throws IllegalStateException если вызвано после запроса опционального аргумента
         *      или массива аргументов
         *
         * @see Context#getArray(String) получение запрашиваемых аргументов
         */
        public Builder<T> requireArrayArgument(String name) {
            if (!arguments.isEmpty() && arguments.getLast().isArray)
                throw new IllegalStateException("Can't specify arguments after Array argument");

            arguments.add(new Argument(name, false, true));
            return this;
        }

        /**
         * Позволяет захватить следующий токен команды как аргумент.<br>
         * Данный токен может отсутствовать.
         * <br>
         * <br>Пример:
         * <pre><code>
         * proc.registerCommand("invite", (a)->a
         *     .findArgument("username")
         *     .executes((ctx)->{
         *         ctx.out.println(ctx.getString("username"))
         *     })
         * );
         * </code></pre>
         * <pre><code>
         * > /invite
         * NoSuchElementException: blah blah blah
         *        at Context.getString(String argumentName)
         *        at blah blah
         *        at blah blah blah
         * > /invite Flory
         * Flory
         * </code></pre>
         * <br>
         * Советую хорошо называть аргументы, так как <code>name</code> будет выводиться
         * при вызове /help.
         *
         * @param name название аргумента
         * @throws IllegalStateException если вызвано после запроса массива аргументов
         *
         * @see Context#hasArgument(String) проверка на наличие аргумента
         * @see Context#getString(String) получение запрашиваемых аргументов
         */
        public Builder<T> findArgument(String name) {
            if (!arguments.isEmpty() && arguments.getLast().isOptional)
                throw new IllegalStateException("Can't specify arguments after Array argument");
            if (!arguments.isEmpty() && arguments.getLast().isArray)
                throw new IllegalStateException("Can't specify arguments after Array argument");

            arguments.add(new Argument(name, true));
            return this;
        }

        /**
         * Отделяет ветку от текущей команды с заданным именем.<br>
         * <br>
         * Пример:
         * <pre><code>
         *     .register("base", (a)->a
         *          .subcommand("subcommand1" (b)->b
         *              .executes(()->{})
         *          )
         *          .subcommand("subcommand2" (b)->b
         *              .executes(()->{})
         *          )
         *     )
         * </code></pre>
         * <pre><code>
         *     > /base
         *     Further subcommands expected
         *     /base__________
         *          ^^^^^^^^^^
         *     > /base subcommand1
         *     > /base subcommand2
         *     > /base subcommand3
         *     Unknown subcommand
         *     /base subcommand3
         *           ^^^^^^^^^^^
         * </code></pre>
         * Для работы команды <code>/base</code> добавьте рядом с декларациями субкоманд executes
         * <pre><code>
         * .register("base", (a)->a
         *     .executes(()->{})
         *     .subcommand("subcommand1" (b)->b
         *         ...
         *     ...
         * )
         * </code></pre>
         * <pre><code>
         *     > /base
         *     > /base subcommand1
         *     > /base subcommand2
         * </code></pre>
         *
         * @throws IllegalArgumentException если суб-команда с этим названием уже есть
         */
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
         * <pre><code>
         *     .register("command", (a)->a
         *         .executes(()->{
         *             System.out.println("Hola.")
         *         })
         *     );
         * </code></pre>
         * <pre><code>
         *     > /command
         *     Hola.
         * </code></pre>
         * Выводить в System.out не стоит. Код написан для примера.<br>
         * См. {@link #executes(Apply)}
         *
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
         * при успешном выполнении команды.<br>
         * Дает доступ к {@link Context контексту} команды
         *
         * <pre><code>
         *     .register("command", (a)->a
         *         .executes((ctx)->{
         *             ctx.out.println("Hola.")
         *             ctx.out.println(ctx.hasArgument("arg"))
         *         })
         *     );
         * </code></pre>
         * <pre><code>
         *     > /command
         *     Hola.
         *     false
         * </code></pre>
         *
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
                isPhantom,
                isInvisible
            );
        }
    }
}
