package client.elements;

import client.elements.cli.ServerRequestCommands;
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

    public static String host;
    public static int port;

    public static SimpleSocket socket = null;

    public static void send(String msg) {
        if (isConnected())
            socket.sendln(msg);
        else
            System.err.println("Not connected to server.");
    }

    static boolean isConnected() {
        return socket != null;
    }

    /**
     * Создает, если это возможно, соединение с сервером и начинает прослушивать сообщения.
     */
    public static void connect() {
        socket = new SimpleSocket(host, port);
        if (socket.isClosed())
            socket = null;
        else {
            System.out.println("Connected to the server");
            processConnection();
            outputListeners.forEach(it -> it.run("Connected to the server"));
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

    static boolean isDisconnected() {
        return socket == null;
    }

    /**
     * Создание потока для подключения к серверу.
     */
    public static void processConnection() {
        new Thread(() -> {
            while (isConnected()) {
                if (socket.hasNewMessage()) {
                    var message = socket.receiveMessage();

                    // Сервер прислал запрос. Отвечаем и ничего не выводим пользователю.
                    if (ServerRequestCommands.processor.execute(message) == null)
                        continue;

                    System.out.println(message);
                    outputListeners.forEach(it -> it.run(message));
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
}
