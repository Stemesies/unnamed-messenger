package utils.cli;

import utils.kt.ApplyStrict;

/**
 * Процессор команд, позволяющий обрабатывать поступающие команды.
 * <br> Для добавления команд их нужно зарегистрировать
 * с помощью {@link #register(String, ApplyStrict)}. Тогда они станут
 * доступны для исполнения и будут отображаться в меню /help.
 * <br>
 * <br> Пример:
 * <pre><code>
 *     var processor = new CommandProcessor();
 *     processor.register("hai", (a) -> a
 *         .executes(()-> System.out.println("Hello."));
 *     );
 *     processor.execute("/hai");
 *     processor.execute("/help");
 * </code></pre>
 * <pre><code>
 *     > /hai
 *     Hello.
 * </code></pre>
 * Команда /help вшита изначально.
 * <pre><code>
 *     > /help
 *     /help [subcommand] - выводит все доступные команды
 *     /hai
 * </code></pre>
 *
 * <br> Чтобы получить результат выполнения команды, можете использовать {@link #getOutput()}
 * <pre><code>
 *     processor.execute("/hai");
 *     System.out.printf("\"%s\"", processor.getOutput());
 * </code></pre>
 * <pre><code>
 *     "Hello."
 * </code></pre>
 *
 * <br> Команда может завершиться неудачно по разным причинам, будь то неправильный синтаксис,
 * неизвестная команда, отсутствие необходимых аргументов.
 * <br>Для получения ошибки можете использовать {@link #getLastError()}.
 * <br> Для определения типа ошибки подходит {@link CommandError#type}.
 * <br>Посмотреть все типы можно в {@link CommandErrors}.
 *
 */
public class CommandProcessor extends CustomCommandProcessor<Object> {
    /**
     * Исполняет команду.
     * <br>При ошибке вы можете получить информацию с помощью
     * {@link CommandProcessor#getLastError()}.
     *
     * @return true, если команда была успешно выполнена.
     *     <br>false, если возникла ошибка.
     */
    public CommandError execute(String input) {
        return super.execute(input, new Object());
    }

    public boolean executeAndExplain(String input) {
        return super.executeAndExplain(input, new Object());
    }
}
