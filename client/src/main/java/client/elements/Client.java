package client.elements;

import utils.elements.ClientTypes;

import java.util.ArrayList;
import java.util.HashMap;

public class Client {

    public static String openChatId;

    private static ClientTypes type;

    public static void setType(ClientTypes type) {
        Client.type = type;
    }

    static ServerConnectManager scm = new ServerConnectManager("127.0.0.1", 8080);

    public static InputManager inputManager = new InputManager();

    public static HashMap<String, ArrayList<String>> map = new HashMap<>(20);

    public static void launch(ClientTypes type) {
        scm.connect();
        System.out.println("Я жив!");
        setType(type);
    }

    /**
     * Добавляет непрочитанное сообщение в контейнер непрочитанных. <br>
     * Сохраняет данные по ключу #groupName
     *
     * @param groupName - "строковый" id группы
     * @param msg - новое сообщение
     */
    public static void addUnreadMsg(String groupName, Object msg) {
        ArrayList<String> unread = map.get(groupName);
        unread.add(msg.toString());
        map.put(groupName, unread);
    }

    /**
     * Удаляет сообщения из непрочитанных при открытии группы.
     *
     * @param groupName - "строковый" id открытого чата
     */
    public static void readMessage(String groupName) {
        map.remove(groupName);
    }
}
