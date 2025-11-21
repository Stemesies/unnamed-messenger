package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandProcessor {
    ArrayList<Command> commands = new ArrayList<>();
    static final Pattern pattern = Pattern.compile(
        "^/|(\\w+|\"\"|\".*?(?:(?<=[^\\\\])(?:\\\\\\\\)+|[^\\\\])\")"
            + "|"
            + "([^\"^[:alnum]]+?(?=\\w|\"|$)|\".*)"
    );

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public boolean processCommand(String command) {
        if (command.charAt(0) != '/')
            return false; // Possibly is not command.

        var validatorResult = CommandValidator.validate(command);
        if (validatorResult != null) {
            System.out.println(validatorResult.getMessage());
            return true;
        }

        List<String> tokens = CommandTokenizer.tokenize(command);
        System.out.println(tokens);
        return true;
    }
}
