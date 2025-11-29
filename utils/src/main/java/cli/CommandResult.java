package cli;

import utils.Ansi;

public class CommandResult {

    public final String message;
    public final CommandResults type;
    public final int start;
    public final int end;

    CommandResult(CommandResults type, String command, int start, int end, Object... args) {
        this.type = type;
        this.start = start;
        this.end = end;

        message = highlightError(type.getMessage().formatted(args), command, start, end);

    }

    /**
     * .
     *
     * @param command Команда для отображения. Может быть null
     * @param type тип ошибки (см. {@link CommandResults})
     * @param args аргументы для форматирования строки, если таковые нужны
     */
    public CommandResult(String command, CommandResults type, Object... args) {
        this.type = type;
        this.start = 0;
        this.end = 0;

        message = Ansi.applyStyle(type.getMessage().formatted(args), Ansi.Colors.RED)
            + (command == null ? "" : "\n" + command);
    }

    CommandResult(CommandResults type, String command, Token token, Object... args) {
        this(type, command, token.start(), token.end(), args);
    }

    /**
     * .
     *
     * @param type тип ошибки (см. {@link CommandResults})
     */
    public CommandResult(CommandResults type, Command.Context context, Object... args) {
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

    public void explain() {
        System.out.println(message);
    }
}
