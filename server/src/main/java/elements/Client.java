package elements;

import network.SimpleSocket;
import utils.Ansi;
import utils.Utils;
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

    public void sendMessageToChat(String message) {
        if (user != null) {
            if (group == null) {
                sendln(Ansi.Colors.RED.apply("No group opened."));
                return;
            }
            var chatMessage = Utils.createChatMessage(this, message);

            System.out.println(group.groupname + ": " + chatMessage);
            group.messages.add(new Message(0, chatMessage, 0, null));
            for (var u : group.members) {
                var client = CollectionExt.findBy(ServerData.getClients(), (it) -> it.user.id == u);
                if (client == null)
                    continue;
                client.sendln(chatMessage);
            }
        } else {
            sendln(Ansi.Colors.RED.apply("You aren't logged in."));
        }
    }
}
