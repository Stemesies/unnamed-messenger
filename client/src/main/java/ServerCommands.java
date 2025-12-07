import cli.CustomCommandProcessor;
import elements.Client;
import elements.Group;
import elements.User;
import network.SimpleSocket;
import requests.FriendRequest;

import java.util.Scanner;

@SuppressWarnings("checkstyle:LineLength")
public class ServerCommands {

    public static class ServerContextData {
        public Client client;
        public User user;
        public Group group;
        public FriendRequest request;
        public SimpleSocket socket;

        public ServerContextData(/*Client client, User user, Group group, FriendRequest request, SimpleSocket socket*/) {
//            this.client = client;
//            this.user = user;
//            this.group = group;
//            this.request = request;
//            this.socket = socket;
        }
    }

    public static final CustomCommandProcessor<ServerContextData> processor = new CustomCommandProcessor<>();

    public static ServerContextData data = new ServerContextData(/*clientMain, clientMain.user, clientMain.group*/);

    static void registerMsg() {
        System.out.println("you are logged out! Please, log in or register your account.");
    }

    void friendRequestMsg() {

        System.out.printf("You received a friend request from user %s\n! Are you want to approve it?",
                data.user.getUserName());
        Scanner responseInput = new Scanner(System.in);
        String resp = "";

        if (responseInput.hasNextLine()) {
            resp = responseInput.nextLine();

            if (resp.equals("y")) {
                data.request.requested = true;
                data.request.isResponsed = true;
            } else if (resp.equals("n")) {
                data.request.requested = false;
                data.request.isResponsed = true;
            } else {
                data.request.isResponsed = false;
            }
        }

        responseInput.close();
    }

    void friendAddMsg() {
        System.out.println("Your friend request is approved!");
    }

    void friendCancelMsg() {
        System.out.println("Your friend request is cancel.");
    }

    void deleteFriendMsg() {
        System.out.println("User has deleted from your friends.");
    }

    static void newMessageMsg() {
        System.out.println("You have new massage!");
    }

    private static void initFriendResponse() {
        processor.register("friends", (a) -> a
                .description("Add user to your friends or not?")
                .subcommand("add", (b) -> b
                        .description("Add user to friends")
                        .executes((success) -> {
                            System.out.println(success.getString("argumentName"));
                        }))
                .requireArgument("argumentName")
        );
        processor.register("friends", (a) -> a
                .description("Delete friend")
                .subcommand("del", (b) -> b
                        .executes((deletion) -> {
                            System.out.println(deletion.getString("argument"));
                        })
                ).requireArgument("argument")
        );

    }

    private static void initRegisterResponse() {
        processor.register("register", (a) -> a
                .description("Register message")
                .subcommand("request", (b) -> b
                        .executes(ServerCommands::registerMsg))
        );
    }

    public static void initGroupResponse() {
        processor.register("chat", (a) -> a
                .description("Send id of open chat.")
                .subcommand("fetch", (b) -> b
                        .executes(() -> {
                            data.socket.sendMessage("/response chat" + data.group.getIdGroup());
                        })
                )
        );
        processor.register("chat", (a) -> a
                .description("Add new message to unread.")
                .subcommand("new", (b) -> b
                        .executes((msg) -> {
                            if (!data.group.getGroupName().equals(msg.getString("groupId"))) {
                                data.client.addUnreadMsg(msg.getString("groupId"), msg.getString("message"));
                                newMessageMsg();
                            }
                        })
                        .requireArgument("groupId")
                        .requireArgument("message")
                )
        );
    }

    public static void initGeneral() {
        initFriendResponse();
        initRegisterResponse();
        initGroupResponse();
    }
}
