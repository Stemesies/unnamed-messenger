package cli;

import cli.utils.Token;
import utils.StringPrintWriter;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class Context<T> {

    public StringPrintWriter out;
    public HashMap<String, Token> arguments = new HashMap<>();
    public T data;

    String command;
    List<Token> tokens;
    int position = 0;

    public Context(
        StringPrintWriter out,
        List<Token> tokens,
        String command,
        T data
    ) {
        this.out = out;
        this.tokens = tokens;
        this.command = command;
        this.data = data;
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