package utils.cli;

import utils.cli.utils.Token;
import utils.Ansi;

public class CommandError {

    public final String message;
    public final CommandErrors type;
    public final int start;
    public final int end;

    CommandError(CommandErrors type, String command, int start, int end, Object... args) {
        this.type = type;
        this.start = start;
        this.end = end;

        message = highlightError(type.getMessage().formatted(args), command, start, end);

    }

    /**
     * .
     *
     * @param command Команда для отображения. Может быть null
     * @param type тип ошибки (см. {@link CommandErrors})
     * @param args аргументы для форматирования строки, если таковые нужны
     */
    public CommandError(String command, CommandErrors type, Object... args) {
        this.type = type;
        this.start = 0;
        this.end = 0;

        message = Ansi.applyStyle(type.getMessage().formatted(args), Ansi.Colors.RED)
            + (command == null ? "" : "\n" + command);
    }

    CommandError(CommandErrors type, String command, Token token, Object... args) {
        this(type, command, token.start(), token.end(), args);
    }

    /**
     * .
     *
     * @param type тип ошибки (см. {@link CommandErrors})
     */
    public CommandError(CommandErrors type, Context<?> context, Object... args) {
        this(type, context.command, context.currentToken(), args);
    }

    private static String highlightError(String msg, String command, int start, int end) {
        var cl = command.length();

        var leftPos = Math.min(start, cl);
        var rightPos = Math.min(end, cl);

        var goesOutBounds = start > cl || end > cl;
        var extension = goesOutBounds
            ? " ".repeat(end - start)
            : "";

        var errorSentence = Ansi.applyStyle(
            command.substring(leftPos, rightPos) + extension,
            Ansi.Colors.RED.and(Ansi.Modes.UNDERLINE)
        );

        var rightSentence = Ansi.applyStyle(
            goesOutBounds ? "" : command.substring(end),
            Ansi.Colors.RED
        );

        return Ansi.applyStyle(msg, Ansi.Colors.RED)
            + '\n'
            + command.substring(0, leftPos)
            + errorSentence
            + rightSentence
            + '\n'
            + " ".repeat(leftPos)
            + Ansi.applyStyle("^".repeat(end - start), Ansi.Colors.RED);
    }

    @Override
    public String toString() {
        return message;
    }

    public void explain() {
        System.out.println(message);
    }
}
