package client.elements;

import client.elements.cli.ServersideCommands;
import utils.cli.CommandProcessor;

import java.util.Scanner;

import static utils.cli.CommandErrors.PHANTOM_COMMAND;

public class InputManager {

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private final Scanner in = new Scanner(System.in);

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

    public void startInputThread() {
        new Thread(() -> {
            while (true) {
                if (!in.hasNextLine()) {
                    exit();
                    return;
                }
                var msg = in.nextLine();
                processInput(msg);
            }
        }).start();

    }

    /**
     * Получение сообщения от клиента.
     */
    @SuppressWarnings("checkstyle:LineLength")
    public void processInput(String msg) {
        if (msg.charAt(0) == '/') {
            commandProcessor.execute(msg, null);
            var procError = commandProcessor.getLastError();
            var procOutput = commandProcessor.getOutput();
            if (procError != null) {
                if (procError.type == PHANTOM_COMMAND)
                    send(msg);
                else
                    procError.explain();
            } else if (!procOutput.isEmpty())
                System.out.print(procOutput);

            return;
        }

        send(msg);

    }

    private void send(String msg) {
        if (isConnected())
            ServerConnectManager.socket.sendln(msg);
        else
            System.err.println("Not connected to server.");
    }
}
