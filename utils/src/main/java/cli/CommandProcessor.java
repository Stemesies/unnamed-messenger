package cli;

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
}
