package cli;

import utils.Ansi;

public class IllegalCommandResult {
    public final String message;
    public final String type;
    public final int start;
    public final int end;

    public IllegalCommandResult(String msg, String command, int start, int end) {
        type = msg;
        message = highlightError(msg, command, start, end);
        this.start = start;
        this.end = end;
    }

    public IllegalCommandResult(String msg, String command, Token token) {
        this(msg, command, token.start(), token.end());
    }

    public IllegalCommandResult(String msg, Command.Context context) {
        this(msg, context.command, context.currentToken());
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
