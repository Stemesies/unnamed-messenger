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

    public ClientTypes type = ClientTypes.GUI;
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
     * Отправляет отформатированное сообщение в зависимости от типа клиента.
     */
    public void styledSendln(Object message, Ansi style, boolean isHtml)
        throws IllegalStateException {
        if (isHtml) {
            socket.sendln(Ansi.applyHtml(message, style));
        } else {
            socket.sendln(Ansi.applyStyle(message, style));
        }
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

    public void sendMessageToChat(String content, boolean isHtml) {
        if (user == null) {
            styledSendln(("You aren't logged in."), Ansi.Colors.RED, true);
            return;
        }
        if (group == null) {
            styledSendln("No group opened.", Ansi.Colors.RED, type == ClientTypes.GUI);
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

            if (client != this) {
                client.sendln(strMessage);
            } else {
                if (strMessage.length() <= 30)
                    client.styledSendln(Message.getOffset(strMessage), Ansi.Colors.YELLOW,
                            isHtml);
                else {
                    var partMsg = strMessage;
                    var last = strMessage;
                    for (int i = 0; !last.isEmpty(); i += 30) {
                        if (last.length() < 30) {
                            client.styledSendln(Message.getOffset(last), Ansi.Colors.YELLOW,
                                    isHtml);
                            break;
                        } else {
                            partMsg = last.substring(0, 31);
                            last = last.substring(31, last.length());

                            client.styledSendln(Message.getOffset(partMsg), Ansi.Colors.YELLOW,
                                    isHtml);
                        }
                    }
                }
            }
        }
    }

    public void stateRequest() {
        // noinspection SwitchStatementWithTooFewBranches
        switch (state) {
            case AwaitingType -> sendln("/request type");
            default -> {
            }
        }
    }
}
