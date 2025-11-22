package cli;

import utils.Apply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class Command {

    final String baseCommand;
    private final List<Command> subcommands;
    private final List<Argument> arguments;

    final Apply<Context> action;
    final Condition condition;

    private Command(
        String baseCommand,
        List<Command> subcommands,
        List<Argument> arguments,
        Apply<Context> action,
        Condition condition
    ) {
        this.baseCommand = baseCommand;
        this.subcommands = subcommands;
        this.arguments = arguments;
        this.action = action;
        this.condition = condition;
    }

    public static Builder create(String baseCommand) {
        return new Builder(baseCommand);
    }

    private IllegalCommandResult consumeArguments(Context context, int limit) {
        context.position++;
        for (var argument : arguments) {

            if (context.position > limit) {
                // Мы дошли до следующий суб-команды
                if (!argument.isOptional) {

                    // Не все необходимые аргументы были введены
                    return new IllegalCommandResult(
                        "Missing required argument \"" + argument.name + "\".",
                        context.command,
                        context.token(context.position - 1).end(),
                        context.currentToken() == null
                            ? context.command.length() + 10
                            : context.currentToken().end()
                    );
                }
                return null;
            }

            context.arguments.put(argument.name, context.consumeToken());
        }
        return null;
    }

    public IllegalCommandResult execute(Context context) {
        var token = context.currentToken();
        if (token == null)
            throw new NullPointerException("Null token. Position %d. Available [0;%d)."
                .formatted(context.tokens.size(), context.position)
            );

        if (!token.is(baseCommand))
            return new IllegalCommandResult("Invalid token:", context);


        // Ищем позицию, на которой располагается следующая суб-команда:
        var subcommandPos = context.position;
        Command foundSubcommand = null;
        while (context.tokens.size() > ++subcommandPos) {
            for (var subcommand : subcommands) {
                if (context.token(subcommandPos).isFunctional(subcommand.baseCommand)) {
                    foundSubcommand = subcommand;
                    break;
                }
            }
        }

        // Смотрим и забираем все аргументы, следующие перед найденной суб-командой
        var argumentConsumptionResult = consumeArguments(context, subcommandPos - 2);
        if (argumentConsumptionResult != null)
            return argumentConsumptionResult;

        if (foundSubcommand != null) {
            return foundSubcommand.execute(context);
        }

        if (action == null) {
            if (context.currentToken() != null)
                return new IllegalCommandResult(
                    "Invalid subcommand.",
                    context
                );

            var lastToken = context.currentToken();
            return new IllegalCommandResult(
                "Further subcommands expected.",
                context.command,
                lastToken == null ? context.command.length() : lastToken.end(),
                context.command.length() + 10
            );
        }

        action.run(context);

        return null;

    }

    // -------

    // TODO: migrate to user and group classes after they're done.
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

        Token token(int index) {
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
            return token(position++);
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

    @FunctionalInterface
    public interface Condition {
        boolean check();
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
        Condition condition = null;

        public Builder(String baseCommand) {
            this.baseCommand = baseCommand;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param condition условие прохода
         */
        public Builder require(Command.Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Позволяет захватить следующий токен команды как аргумент.
         * <br>Если токен не был найден - будет выведена ошибка при выполнении команды.
         * <br>
         * <br>Пример:
         * <pre><code>
         * (...).subcommand("invite").requireArgument(username)
         *
         * /groups invite Flory (Выполнение успешно)
         *
         * Отсутствует поле
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

        public Builder subcommand(String subcommand, Apply<Builder> subcommandSettings) {
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
                condition
            );
        }
    }
}
