package client.elements;

import utils.cli.CommandProcessor;
import utils.kt.Apply;
import utils.network.SimpleSocket;

import java.util.ArrayList;
import java.util.List;

public class ServerConnectManager {
    private static final List<Apply<String>> outputListeners = new ArrayList<>();

    public static void addOutPutListener(Apply<String> listener) {
        outputListeners.add(listener);
    }

    public final String host;
    public final int port;

    public String message;

    public static SimpleSocket socket = null;
    private final CommandProcessor commandProcessor = new CommandProcessor();

    public ServerConnectManager(String host, int port) {
        this.host = host;
        this.port = port;
        registerClientsideCommands();
    }

    boolean isConnected() {
        return socket != null;
    }

    /**
     * Создает, если это возможно, соединение с сервером и начинает прослушивать сообщения.
     */
    public void connect() {
        socket = new SimpleSocket(host, port);
        if (socket.isClosed())
            socket = null;
        else {
            System.out.println("Connected to the server");
            this.message = "Connected";
            processConnection();
            System.out.println("Здесь лежит не нулл! " + this.message);
            outputListeners.forEach(it -> it.run(this.message));
//            updateControllerMsg();
//            System.out.println("Он должен быть в строке: " + HelloController.getMsg());
        }
    }

    /**
     * Разрывает соединение с сервером, если таковое имеется. <br>
     * Все потоки, работающие с ним также будут автоматически остановлены.
     */
    public static void disconnect() {
        if (socket == null)
            return;

        socket.close();
        socket = null;
        System.out.println("Disconnected from the server");
    }

    boolean isDisconnected() {
        return socket == null;
    }

    /**
     * Создание потока для подключения к серверу.
     */
    public void processConnection() {
        new Thread(() -> {
            while (isConnected()) {
                if (socket.hasNewMessage()) {
                    this.message = socket.receiveMessage();
                    System.out.println(this.message);
                    outputListeners.forEach(it -> it.run(this.message));
//                    updateControllerMsg();
                } else {
                    disconnect();
                    break;
                }
            }
        }).start();
    }

    public static void exit() {
        disconnect();
        System.exit(0);
    }

    /**
     * Регистрирует команды для соединения с сервером.
     */
    private void registerClientsideCommands() {

        commandProcessor.register("exit", (it) -> it
                .executes(ServerConnectManager::exit)
        );
        commandProcessor.register("retry", (it) -> it
                .require("Already connected.", this::isDisconnected)
                .executes(this::connect)
        );
    }
}
