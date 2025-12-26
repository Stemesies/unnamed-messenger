package client.elements;

import client.elements.cli.ServersideCommands;
import utils.Ansi;
import utils.cli.CommandProcessor;

import java.util.Scanner;

import static client.elements.ServerConnectManager.exit;
import static utils.cli.CommandErrors.PHANTOM_COMMAND;

public class InputManager {

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private final Scanner in = new Scanner(System.in);

    public InputManager() {
        ServersideCommands.init(commandProcessor);
        registerClientsideCommands();
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
        if (msg == null || msg.isEmpty())
            return;
        if (msg.charAt(0) == '/') {
            commandProcessor.execute(msg, null);
            var procError = commandProcessor.getLastError();
            var procOutput = commandProcessor.getOutput();
            if (procError != null) {
                if (procError.type == PHANTOM_COMMAND)
                    ServerConnectManager.send(msg);
                else
                    procError.explain();
            } else if (!procOutput.isEmpty())
                System.out.print(procOutput);

            return;
        }

        ServerConnectManager.send(msg);

    }

    /**
     * Регистрирует команды для соединения с сервером.
     */
    private void registerClientsideCommands() {

        commandProcessor.register("exit", (it) -> it
            .executes(ServerConnectManager::exit)
        );
        commandProcessor.register("retry", (it) -> it
            .require("Already connected.", ServerConnectManager::isDisconnected)
            .executes(ServerConnectManager::connect)
        );
    }
}
