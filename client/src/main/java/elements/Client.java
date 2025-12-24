package elements;

import java.util.ArrayList;
import java.util.HashMap;

public class Client {

    public static String openChatId;

    static ServerConnectManager scm = new ServerConnectManager("127.0.0.1", 8080);

    public static HashMap<String, ArrayList<String>> map = new HashMap<>(20);

    public static void launch() {
        scm.connect();
        System.out.println("Я жив!");
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
