package cli;

import utils.Apply;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandProcessor {
    private IllegalCommandResult lastError = null;

    ArrayList<Command> commands = new ArrayList<>();
    static final Pattern pattern = Pattern.compile(
        "^/|(\\w+|\"\"|\".*?(?:(?<=[^\\\\])(?:\\\\\\\\)+|[^\\\\])\")"
            + "|"
            + "([^\"^[:alnum]]+?(?=\\w|\"|$)|\".*)"
    );

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void registerCommand(String command, Apply<Command.Builder> action) {
        var c = Command.create(command);
        action.run(c);
        commands.add(c.build());

    }

    public boolean processCommand(String input) {
        if (input.charAt(0) != '/')
            return false; // Possibly is not command.

        var validatorResult = CommandValidator.validate(input);
        if (validatorResult != null) {
            validatorResult.explain();
            return true;
        }

        List<Token> tokens = CommandTokenizer.tokenize(input);

        for (var command : commands) {
            if (command.baseCommand.equals(tokens.getFirst().content())) {
                lastError = command.execute(new Command.Context(tokens, input, "", ""));

                if (lastError != null)
                    lastError.explain();

                return true;
            }
        }

        lastError = new IllegalCommandResult(
            "Command not found.",
            input,
            tokens.getFirst().start(),
            tokens.getFirst().end()
        );
        lastError.explain();

        return true;
    }

    public IllegalCommandResult getLastError() {
        return lastError;
    }
}
