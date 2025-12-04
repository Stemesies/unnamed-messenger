package cli;

public enum CommandResults {

    // Validator
    EMPTY_COMMAND("Command is empty."),
    NO_SEPARATION("No separation found."),
    UNCLOSED_QUOTE("Unclosed quoted argument."),
    INVALID_SEPARATOR("Invalid symbol."),
    UNEXPECTED_SYMBOL("Unexpected symbol or symbols."),

    // Processor
    INVALID_TOKEN("Invalid token."),
    NOT_A_COMMAND("Not a command."),
    COMMAND_NOT_FOUND("Command not found."),

    // Executor
    CUSTOM_ERROR("%s"),
    MISSING_REQUIRED_ARGUMENT("Missing required argument <%s>."),
    FURTHER_SUBCOMMANDS_EXPECTED("Further subcommands expected."),
    UNKNOWN_SUBCOMMAND("Unknown subcommand."),
    INVALID_SUBCOMMAND("Invalid subcommand.");

    private final String message;

    CommandResults(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public CommandResult create(String command, int start, int end) {
        return new CommandResult(this, command, start, end);
    }

    public CommandResult create(String command, Token token) {
        return new CommandResult(this, command, token.start(), token.end());
    }

    public CommandResult create(Command.Context context) {
        return create(context.command, context.currentToken());
    }
}
