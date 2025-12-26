package server.elements;

import utils.network.SimpleSocket;
import utils.Ansi;
import utils.Utils;
import utils.elements.Message;
import utils.extensions.CollectionExt;

/**
 * Репрезентация клиента со стороны сервера.
 * <br>Содержит в себе всю информацию о подключении,
 * привязку пользователя и т.д.
 */
public class Client {

    /**
     * Текущий привязанный профиль пользователя.
     * <br>
     * <br><code>null</code> - если на клиенте
     * не был произведен вход / регистрация.
     */
    public User user = null;

    @Deprecated
    public Group group = null;

    private final SimpleSocket socket;

    public Client(SimpleSocket socket) {
        this.socket = socket;
    }

    public void close() {
        socket.close();
    }

    /**
     * Отправляет сообщение клиенту.
     *
     * @see SimpleSocket#sendln(String)
     */
    public void sendln(Object message) throws IllegalStateException {
        socket.sendln(message.toString());
    }

    /**
     * Отправляет сообщение клиенту.
     *
     * @see SimpleSocket#sendln(String)
     */
    public void send(Object message) throws IllegalStateException {
        socket.send(message.toString());
    }

    /**
     * Проверяет, есть ли новые сообщения от клиента.
     *
     * @see SimpleSocket#hasNewMessage()
     */
    public boolean hasNewMessage() {
        return socket.hasNewMessage();
    }

    /**
     * Ждет и возвращает сообщение от клиента.
     *
     * @see SimpleSocket#receiveMessage()
     */
    public String receiveMessage() throws IllegalStateException {
        return socket.receiveMessage();
    }

    @Override
    public String toString() {
        return user == null ? super.toString() : user.getName();
    }

    /**
     * Выводит сообщение от себя в чат пользователя.
     *
     * @param message - сообщение в чат
     */
    public String getOffset(String message) {
        StringBuilder offset = new StringBuilder(280);
        for (int i = 0; i < (220 - message.length() * 1.5); i++) {
            offset.append(" ");
        }
        return offset.toString() + message;
    }

    public void sendMessageToChat(String message) {
        if (user == null) {
            sendln(Ansi.Colors.RED.apply("You aren't logged in."));
            return;
        }
        if (group == null) {
            sendln(Ansi.Colors.RED.apply("No group opened."));
            return;
        }
        var chatMessage = Utils.createChatMessage(this, message);

        System.out.println(group.getGroupname() + ": " + chatMessage);
        Message messageContent = new Message(0, chatMessage, user.getUserId(), null);
        group.addMessage(messageContent);
        for (var u : group.getMembersId()) {
            var client = CollectionExt.findBy(ServerData.getClients(),
                    (it) -> it.user.getUserId() == u);
            if (client == null)
                continue;
            if (client != this) {
                client.sendln(chatMessage);
            } else {
                client.sendln(getOffset(chatMessage));
            }
        }
    }
}
