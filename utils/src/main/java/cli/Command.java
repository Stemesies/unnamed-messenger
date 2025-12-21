package cli;

import elements.AbstractGroup;
import elements.AbstractUser;
import utils.kt.Apply;
import utils.kt.Check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static cli.CommandResults.CUSTOM_ERROR;
import static cli.CommandResults.FURTHER_SUBCOMMANDS_EXPECTED;
import static cli.CommandResults.INVALID_SUBCOMMAND;
import static cli.CommandResults.INVALID_TOKEN;
import static cli.CommandResults.MISSING_REQUIRED_ARGUMENT;
import static cli.CommandResults.PHANTOM_COMMAND;
import static cli.CommandResults.UNKNOWN_SUBCOMMAND;

public class Command {

    final String base;
    final String helpDescription;
    final List<Command> subcommands;
    final List<Argument> arguments;

    final Apply<Context> action;
    final List<Condition> conditions;

    final boolean isInvisible;
    final CommandResult isPhantom;

    private Command(
        String base,
        String helpDescription,
        List<Command> subcommands,
        List<Argument> arguments,
        Apply<Context> action,
        List<Condition> conditions,
        CommandResult isPhantom,
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

    public static Builder create(String baseCommand) throws IllegalArgumentException {
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

    /**
     * Класс, позволяющий при исполнении проверок и/или исполнении самой команды
     * обращаться к различным внешним переменным.<br>
     * <br>
     * На данный момент реализовано следующее:
     * <ul>
     * <li>{@link Context#out}</li>
     * <li>{@link Context#hasArgument(String key)}</li>
     * <li>{@link Context#getString(String)}</li>
     * <li>{@link Context#getArray(String)}</li>
     * </ul>
     * TODO:
     */
    public static class Context {

        /**
         * "Поток" вывода команды, {@link CommandProcessor#getOutput() который можно получить позже}
         * <br>Логи сюда писать не стоит.
         *
         * <pre><code>
         *     .register("someCommand", ...
         *     ...
         *     .executes((ctx)-> {
         *         //Какие-нибудь действия
         *         ctx.out.println("Успешно выполнено!")
         *     })
         * </code></pre>
         *
         * <pre><code>
         *     processor.execute("/someCommand")
         *     System.out.println(processor.getOutput())
         *     > Успешно выполнено!
         * </code></pre>
         */
        public final StringPrintWriter out;
        private final HashMap<String, Object> arguments = new HashMap<>();

        String command;
        AbstractUser user;
        AbstractGroup group;

        List<Token> tokens;
        int position = 0;

        public Context(
            StringPrintWriter out,
            List<Token> tokens,
            String command,
            AbstractUser user,
            AbstractGroup group
        ) {
            this.out = out;
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

        /**
         * Проверяет, вводил ли пользователь аргумент argumentName.
         * <br>Данным методом проверять стоит только опциональные аргументы.
         * <br>(см. {@link Builder#findArgument(String) запрос опционального аргумента})
         *
         * <pre><code>
         *     .registerCommand("hello", (a)->a
         *         .findArgument("username")
         *         .executes((ctx)->{
         *             if(ctx.hasArgument("username")) {
         *                 ctx.out.println("Hello, " + ctx.getString("username"));
         *             } else {
         *                 ctx.out.println("Hello!")
         *             }
         *         })
         *     )
         * </code></pre>
         *
         * <pre><code>
         *      > /hello Buteo
         *      Hello, Buteo
         *      > /hello Flory
         *      Hello, Flory
         *      > /hello
         *      Hello!
         * </code></pre>
         *
         * @param argumentName название проверяемого аргумента.
         * @return <code>true</code>, если аргумент был введен
         *     <br><code>false</code> - если не был
         *
         * @see #getString(String)
         * @see #out
         */
        public boolean hasArgument(String argumentName) {
            return arguments.containsKey(argumentName);
        }

        /**
         * Получает введенный пользователем аргумент.
         *
         * <pre><code>
         *     .registerCommand("hello", (a)->a
         *         .requireArgument("username")
         *         .executes((ctx)->{
         *              ctx.out.println("Hello, " + ctx.getString("username"));
         *         })
         *     )
         * </code></pre>
         *
         * <pre><code>
         *      > /hello Buteo
         *      Hello, Buteo
         *      > /hello Flory
         *      Hello, Flory
         *      > /hello Stemie
         *      Hello, Stemie
         * </code></pre>
         *
         * @param argumentName название получаемого аргумента
         * @return значение получаемого аргумента
         * @throws NoSuchElementException если применен к отсутствующему опциональному аргументу.
         *      <br>Чтобы избежать ошибок, см. {@link #hasArgument(String)}
         * @throws IllegalArgumentException если применен к массиву аргументов.
         *      <br>Чтобы избежать ошибок, см. {@link #getArray(String)}
         */
        public String getString(String argumentName)
            throws NoSuchElementException, IllegalArgumentException {
            var argument = arguments.get(argumentName);
            if (argument == null)
                throw new NoSuchElementException(
                    "No arguments with name \"" + argumentName + "\" found."
                );
            if (argument instanceof Token)
                return ((Token) argument).content();
            else throw new IllegalArgumentException(
                "Tried to read String from Array argument \"" + argumentName + "\"."
            );
        }

        /**
         * Получает введенный пользователем массив аргументов.
         *
         * <pre><code>
         *     .registerCommand("hello", (a)->a
         *         .requireArrayArgument("usernames")
         *         .executes((ctx)->{
         *              if(ctx.getArray("usernames").isEmpty()) {
         *                  ctx.println("Hello!")
         *              } else {
         *                  ctx.print("Hello")
         *                  ctx.getArray("usernames").forEach((it)->{
         *                      ctx.print(", " + it)
         *                  })
         *                  ctx.println()
         *              }
         *         })
         *     )
         * </code></pre>
         *
         * <pre><code>
         *      > /hello
         *      Hello!
         *      > /hello Buteo
         *      Hello, Buteo
         *      > /hello Flory Stemie
         *      Hello, Flory, Stemie
         *      > /hello a b c d e f
         *      Hello, a, b, c, d, e, f
         * </code></pre>
         *
         * @param argumentName название получаемого массива аргументов
         * @return список введенных аргументов
         * @throws IllegalArgumentException если применен к обычному аргументу.
         *      <br>Чтобы избежать ошибок, см. {@link #getString(String)}
         */
        public List<String> getArray(String argumentName)
            throws IllegalArgumentException {
            var argument = arguments.get(argumentName);
            switch (argument) {
                case Token ignored -> throw new IllegalArgumentException(
                    "Tried to read String from Array argument \"" + argumentName + "\".");
                case Object[] tokenArrayList -> {
                    return Arrays.stream((tokenArrayList))
                        .map((it) -> (Token) (it))
                        .map(Token::content)
                        .toList();
                }
                default -> throw new IllegalArgumentException("I fu##ed up somehow.");
            }


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
        boolean isArray = false;

        public Argument(String name, boolean isOptional) {
            this.name = name;
            this.isOptional = isOptional;
        }

        public Argument(String name, boolean isOptional, boolean isArray) {
            this.name = name;
            this.isOptional = isOptional;
            this.isArray = isArray;
        }

        @Override
        public String toString() {
            if (isOptional)
                return "[" + name + "]";
            else
                return "<" + name + ">";
        }
    }

    public static class Builder {

        String baseCommand;
        String helpDescription = null;
        ArrayList<Builder> subcommands = new ArrayList<>();
        ArrayList<Argument> arguments = new ArrayList<>();
        Apply<Context> action = null;
        ArrayList<Condition> conditions = new ArrayList<>();
        CommandResult isPhantom = null;
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
         * процессор будет выдавать особую ошибку {@link CommandResults#PHANTOM_COMMAND}.
         * <br>
         * <br> !! Данный метод не должен применяться к суб-командам !!
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder isPhantom(String msg) throws IllegalStateException {
            if (!isBase)
                throw new IllegalStateException(
                    "isPhantom flag should not be set for subcommands."
                );
            isPhantom = new CommandResult(null, PHANTOM_COMMAND, msg);
            return this;
        }

        /**
         * Позволяет заявить команде /help, что данная команда существует.
         * При этом ее нельзя будет вызвать:
         * процессор будет выдавать особую ошибку {@link CommandResults#PHANTOM_COMMAND}.
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder isPhantom() throws IllegalStateException {
            return isPhantom("Command is unavailable.");
        }

        /**
         * Позволяет заявить команде /help, что данная команда отсутствует,
         * даже если она зарегистрирована.<br>
         * При этом, если команда написана неправильно, будет высвечиваться ошибка
         * {@link CommandResults#COMMAND_NOT_FOUND}
         *
         * @throws IllegalStateException Если метод был применен к суб-командам.
         */
        public Builder isInvisible() throws IllegalStateException {
            if (!isBase)
                throw new IllegalStateException(
                    "isInvisible flag should not be set for subcommands."
                );
            isInvisible = true;
            return this;
        }

        /**
         * Устанавливает описание команды или суб-команды.
         * <pre><code>
         *     .register("someCommand", (a)->
         *         .description("some description~")
         *         .executes(()->{})
         *         .subcommand("subcommand", (a) ->
         *              .description("some subcommand description~")
         *              .executes(()->{})
         *         )
         *     );
         * </code></pre>
         * <pre><code>
         *     > /help
         *     ...
         *     /someCommand - some description~
         *     ...
         *     > /help someCommand
         *     /someCommand - some description~
         *     /someCommand subcommand - some subcommand description~
         * </code></pre>
         *
         * @param helpDescription описание
         */
        public Builder description(String helpDescription) {
            this.helpDescription = helpDescription;
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
        public Builder require(String onFailure, Check condition) throws IllegalStateException {
            if (isPhantom != null)
                throw new IllegalStateException(
                    "Conditions should not be used for phantom commands."
                );

            this.conditions.add(new Condition(onFailure, condition));
            return this;
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
        public Builder requireArgument(String name) throws IllegalStateException {
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
        public Builder requireArrayArgument(String name) {
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
        public Builder findArgument(String name) {
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
        public Builder subcommand(String subcommand, Apply<Builder> subcommandSettings)
            throws IllegalArgumentException {

            for (Command.Builder sb : subcommands) {
                if (subcommand.equals(sb.baseCommand))
                    throw new IllegalArgumentException(
                        "Subcommand '" + subcommand + "' already exists."
                    );
            }

            var sub = new Builder(subcommand);

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
        public Builder executes(Runnable action) throws IllegalStateException {
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
        public Builder executes(Apply<Context> action) throws IllegalStateException {
            if (isPhantom != null)
                throw new IllegalStateException(
                    "Actions should not be used for phantom commands."
                );
            this.action = action;
            return this;
        }

        public Command build() {
            if (subcommands.isEmpty() && action == null && isPhantom == null)
                throw new IllegalStateException(
                    "No subcommand or actions specified for command " + baseCommand + "."
                );

            return new Command(
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
