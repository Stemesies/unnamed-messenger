package client.elements;

import client.elements.cli.ServersideCommands;
import utils.Ansi;
import utils.cli.CommandProcessor;
import utils.elements.ClientTypes;
import utils.kt.Apply;

import java.util.Scanner;

import static client.elements.ServerConnectManager.exit;
import static utils.cli.CommandErrors.PHANTOM_COMMAND;

public class InputManager {

    private static Apply<String> inputInterceptor = null;

    static final CommandProcessor commandProcessor = new CommandProcessor();
    private static final Scanner in = new Scanner(System.in);

    public InputManager() {
        ServersideCommands.init(commandProcessor);
        registerClientsideCommands();
    }

    public static void startInputThread() {
        new Thread(() -> {
            while (true) {
                if (!in.hasNextLine()) {
                    exit();
                    return;
                }
                var msg = in.nextLine();
                Ansi.clearLine();
                processInput(msg);
            }
        }).start();

    }

    /**
     * Получение сообщения от клиента.
     */
    @SuppressWarnings("checkstyle:LineLength")
    public static void processInput(String msg) {
        if (inputInterceptor != null) {
            inputInterceptor.run(msg);
            inputInterceptor = null;
            return;
        }
        if (msg == null || msg.isEmpty())
            return;
        if (msg.charAt(0) == '/') {
            commandProcessor.execute(msg, null);
            var procError = commandProcessor.getLastError();
            var procOutput = commandProcessor.getOutput();
            if (procError != null) {
                if (procError.type == PHANTOM_COMMAND) {
                    ServerConnectManager.send(msg);
                } else {
                    boolean isHtml = Client.getType() != ClientTypes.CONSOLE;
                    OutputManager.println(procError.getMessage(isHtml));
                }
            } else if (!procOutput.isEmpty())
                OutputManager.print(procOutput);

            return;
        }
        ServerConnectManager.send(msg);

    }

    /**
     * Регистрирует команды для соединения с сервером.
     */
    static void registerClientsideCommands() {

        commandProcessor.register("exit", (it) -> it
            .executes(ServerConnectManager::exit)
        );
        commandProcessor.register("retry", (it) -> it
            .require("Already connected.", ServerConnectManager::isDisconnected)
            .executes(ServerConnectManager::connect)
        );
    }

    public static void interceptNextLine(Apply<String> onLine) {
        inputInterceptor = onLine;
    }
}
