package client.elements;

import client.elements.cli.ServerRequestCommands;
import utils.Ansi;

import utils.kt.Apply;
import utils.network.SimpleSocket;

import java.util.ArrayList;
import java.util.List;

public class ServerConnectManager {

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
            OutputManager.stylePrintln("Can't connect to server.", Ansi.Colors.RED);
        } else {
            processConnection();
//            updateControllerMsg();
            OutputManager.stylePrintln("Connected to the server", Ansi.Colors.GREEN);
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
                    if (ServerRequestCommands.processor.execute(message) == null) {
                        continue;
                    }

//                    System.out.println("FUUUUCK");
//                    System.out.println(message);
//                    System.out.println("-------");

                    OutputManager.println(message);
                } else {
                    OutputManager.stylePrintln("Disconnected from the server", Ansi.Colors.RED);
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
