package cli;

import utils.Ansi;

public class IllegalCommandException extends IllegalArgumentException {
    public final int start;
    public final int end;

    public IllegalCommandException(String msg, String command, int start, int end) {

        super(

            Ansi.applyStyle(msg, Ansi.Colors.RED)
                + '\n'
                + command.substring(0, start + 1)
                + Ansi.applyStyle(
                    command.substring(start + 1, end),
                    Ansi.Colors.RED.and(Ansi.Modes.UNDERLINE)
                )
                + Ansi.applyStyle(
                    command.substring(end),
                    Ansi.Colors.RED
                )
                + '\n'
                + " ".repeat(start + 1)
                + Ansi.applyStyle(
                    "^".repeat(end - start - 1),
                    Ansi.Colors.RED
                )

        );

        this.start = start + 1;
        this.end = end;
    }

    public void explain() {
        System.out.println(getMessage());
    }
}
