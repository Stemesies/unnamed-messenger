package utils.cli;

import utils.cli.utils.Token;
import utils.StringPrintWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

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
public class Context<T> {

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
    final HashMap<String, Object> arguments = new HashMap<>();
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

    /**
     * Проверяет, вводил ли пользователь аргумент argumentName.
     * <br>Данным методом проверять стоит только опциональные аргументы.
     * <br>(см. {@link Command.Builder#findArgument(String) запрос опционального аргумента})
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
            throws IllegalArgumentException, NoSuchElementException {
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
