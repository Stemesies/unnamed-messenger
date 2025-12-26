package server.elements;

import utils.elements.ClientTypes;
import utils.network.SimpleSocket;
import utils.Ansi;
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

    public ClientTypes type = null;
    public ClientStates state = ClientStates.Fine;

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

    public void sendMessageToChat(String content) {
        if (user == null) {
            sendln(Ansi.Colors.RED.apply("You aren't logged in."));
            return;
        }
        if (group == null) {
            sendln(Ansi.Colors.RED.apply("No group opened."));
            return;
        }
        Message message = new Message(0, content, user.getName(), user.getId(),  null);
        var strMessage = message.getFormatted();

        System.out.println(group.getGroupname() + ": " + strMessage);

        group.addMessage(message);
        for (var u : group.getMembersId()) {

            var client = CollectionExt.findBy(
                ServerData.getRegisteredClients(),
                (it) -> it.user.getId() == u
            );

            if (client == null)
                continue;

            client.sendln(client != this
                    ? strMessage
                    : message.getFormattedSelf()
            );

        }
    }

    public void stateRequest() {
        // noinspection SwitchStatementWithTooFewBranches
        switch (state) {
            case AwaitingType -> sendln("/request type");
            default -> {}
        }
    }
}
