package client.elements.cli;

import utils.cli.CommandProcessor;
import client.elements.Client;
import client.elements.ServerConnectManager;

@SuppressWarnings("checkstyle:LineLength")
public class ServerCommands {

    public static final CommandProcessor processor = new CommandProcessor();

    /**
     * Сообщение о том, что нужно войти в систему.
     */
    private static void registerMsg() {
        System.out.println("you are logged out! Please, log in or register your account.");
    }

    private void friendAddMsg() {
        System.out.println("Your friend request is approved!");
    }

    /**
     * Уведомление о новом сообщении группы или чата,
     * который не открыт на данный момент.
     */
    private static void newMessageMsg() {
        System.out.println("You have new massage!");
    }

    /**
     * Команды по запросу в друзья, отклонению заявки и пр.
     */
    private static void initFriendResponse() {
        processor.register("friends", (a) -> a
                .description("Friend request")
                .subcommand("add", (b) -> b
                        .description("Add user to friends")
                        .executes((success) -> {
                            System.out.println("You receive a friend request from user "
                                    + success.getString("argument1"));
                        })
                ).requireArgument("argument1")
                .subcommand("del", (d) -> d
                        .executes((deletion) -> {
                            System.out.println("User " + deletion.getString("argument2")
                                    + "deleted from your friends.");
                        })
                )
        );
    }

    /**
     * Команды регистрации пользователя.
     */
    private static void initRegisterResponse() {
        processor.register("register", (a) -> a
                .description("Register message")
                .subcommand("request", (b) -> b
                        .executes(ServerCommands::registerMsg))
        );
        processor.register("require", (client) -> client
                .subcommand("type", (type) -> type
                        .requireArgument("clientType")
                        .executes((whatType) ->
                                ServerConnectManager.socket.sendln("/response type "
                                        + whatType.getString("clientType")
                                )
                        )));
    }

    /**
     * Команды по отправке уведомлений о непрочитанных сообщениях
     * и передаче данных об открытом чате на сервер.
     */
    private static void initGroupResponse() {
        processor.register("chat", (a) -> a
                .description("Send id of open chat.")
                .subcommand("fetch", (b) -> b
                        .executes((c) -> ServerConnectManager.socket.sendln("/response chat " + Client.openChatId))

                        .subcommand("new", (d) -> d
                                .executes((msg) -> {
                                    if (!Client.openChatId.equals(msg.getString("groupId"))) {
                                        Client.addUnreadMsg(msg.getString("groupId"), msg.getString("message"));
                                        newMessageMsg();
                                    } else {
                                        System.out.println(msg.getString("message"));
                                        Client.readMessage(msg.getString("groupId"));
                                    }
                                })
                        ))
                .subcommand("open", (b) -> b
                        .requireArgument("listMessages")
                        .executes((msg) -> {
                            System.out.println(msg.getString("ListMessages"));
                        }))
        );
    }

    /**
     * Регистрация команд сервера на клиенте.
     */
    public static void initGeneral() {
        initFriendResponse();
        initRegisterResponse();
        initGroupResponse();
    }
}
