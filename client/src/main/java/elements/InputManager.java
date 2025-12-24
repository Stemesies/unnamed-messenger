package elements;

import cli.CommandProcessor;
import elements.cli.ServersideCommands;

import static cli.CommandErrors.PHANTOM_COMMAND;

public class InputManager {

    private String input;

    public String getInput() {
        return this.input;
    }

    public void setInput(String in) {
        this.input = in;
    }

    private final CommandProcessor commandProcessor = new CommandProcessor();

    public InputManager() {
        ServersideCommands.init(commandProcessor);
    }

    protected String message;

    boolean isConnected() {
        return ServerConnectManager.socket != null;
    }

    public void exit() {
        disconnect();
        System.exit(0);
    }

    /**
     * Разрывает соединение с сервером, если таковое имеется. <br>
     * Все потоки, работающие с ним также будут автоматически остановлены.
     */
    public void disconnect() {
        if (ServerConnectManager.socket == null)
            return;

        ServerConnectManager.socket.close();
        ServerConnectManager.socket = null;
        System.out.println("Disconnected from the server");
    }

    /**
     * Получение сообщения от клиента.
     */
    @SuppressWarnings("checkstyle:LineLength")
    public void processInput() {
        int i = 0;
        while (i < 1) {
            var msg = this.input;
            i++;

            if (msg.charAt(0) == '/') {
                commandProcessor.execute(msg, null);
                var procError = commandProcessor.getLastError();
                var procOutput = commandProcessor.getOutput();
                if (procError != null) {
                    if (procError.type == PHANTOM_COMMAND)
                        send(msg);
                    else
                        procError.explain();
                } else if (procOutput != null) {
                    System.out.print(procOutput);
                    this.message = procOutput;
                }
                continue;
            }

            send(msg);
        }
    }

    private void send(String msg) {
        if (isConnected())
            ServerConnectManager.socket.sendln(msg);
        else
            System.err.println("Not connected to server.");
    }
}
