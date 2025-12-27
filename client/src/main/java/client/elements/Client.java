package client.elements;

import client.elements.cli.ServerRequestCommands;
import client.elements.cli.ServersideCommands;
import utils.elements.ClientTypes;

import java.util.ArrayList;
import java.util.HashMap;

public class Client {

    public static String openChatId;

    private static ClientTypes type;

    public static ClientTypes getType() {
        return type;
    }

    public static HashMap<String, ArrayList<String>> unread = new HashMap<>(20);

    public static void launch(ClientTypes type) {
        Client.type = type;
        ServerRequestCommands.init();
        ServersideCommands.init(InputManager.commandProcessor);
        InputManager.registerClientsideCommands();

        ServerConnectManager.host = "127.0.0.1";
        ServerConnectManager.port = 8080;

        ServerConnectManager.connect();
    }

    /**
     * Добавляет непрочитанное сообщение в контейнер непрочитанных. <br>
     * Сохраняет данные по ключу #groupName
     *
     * @param groupName - "строковый" id группы
     * @param msg - новое сообщение
     */
    public static void addUnreadMsg(String groupName, Object msg) {
        ArrayList<String> unread = Client.unread.get(groupName);
        unread.add(msg.toString());
    }

    /**
     * Удаляет сообщения из непрочитанных при открытии группы.
     *
     * @param groupName - "строковый" id открытого чата
     */
    public static void readMessage(String groupName) {
        unread.remove(groupName);
    }
}
