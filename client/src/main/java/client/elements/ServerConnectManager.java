package client.elements;

import client.elements.cli.ServerRequestCommands;
import utils.Ansi;

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
        else {
            System.err.println("Not connected to server.");
            OutputManager.stylePrint("Not connected to server.", Ansi.Colors.RED);
        }
    }

    static boolean isConnected() {
        return socket != null;
    }

    /**
     * Создает, если это возможно, соединение с сервером и начинает прослушивать сообщения.
     */
    public static void connect() {
        socket = new SimpleSocket(host, port);

        if (socket.isClosed()) {
            socket = null;
            OutputManager.stylePrint("Can't connect to server.", Ansi.Colors.RED);
        } else {
            System.out.println("Connected to the server");
            processConnection();
            outputListeners.forEach(it -> it.run("Connected to the server"));
//            updateControllerMsg();
            OutputManager.stylePrint("Connected", Ansi.Colors.GREEN);
//            OutputManager.getOutputListeners().forEach(it -> it.run(this.message));
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
        OutputManager.stylePrint("Disconnected from the server", Ansi.Colors.RED);
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

                    if (message.isEmpty())
                        continue;

                    // Сервер прислал запрос. Отвечаем и ничего не выводим пользователю.
                    if (ServerRequestCommands.processor.execute(message) == null)
                        continue;

                    System.out.println(message);
                    outputListeners.forEach(it -> it.run(message));
                    OutputManager.print(message);
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
