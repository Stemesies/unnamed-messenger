import cli.CustomCommandProcessor;
import elements.Client;
import elements.Group;
import elements.User;
import requests.FriendRequest;

import java.util.Scanner;

@SuppressWarnings("checkstyle:LineLength")
public class ServerCommands {

    public static class ServerContextData {
        public Client client;
        public User user;
        public Group group;
        FriendRequest request;

        public ServerContextData(/*Client client, User user, Group group, FriendRequest request*/) {
//            this.client = client;
//            this.user = user;
//            this.group = group;
//            this.request = request;
        }
    }

    public static final CustomCommandProcessor<ServerContextData> processor = new CustomCommandProcessor<>();

    ServerContextData data = new ServerContextData(/*clientMain, clientMain.user, clientMain.group*/);

    void registerMsg() {
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
                this.data.request.requested = true;
                this.data.request.isResponsed = true;
            } else if (resp.equals("n")) {
                this.data.request.requested = false;
                this.data.request.isResponsed = true;
            } else {
                this.data.request.isResponsed = false;
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

    void sendMsgIntoChat() {
        System.out.printf("Your message was send by chat %s\n", data.group.getGroupName());
    }

    void initFriendResponse() {
        processor.register("friend", (a) -> a
                .description("add user to your friends or not?")
                .subcommand("add", (b) -> b
                        .description("Add user to friends")
                        .executes((success) -> {
                            System.out.println(success.getString("argumentName"));
                        }))
                .requireArgument("argumentName")
        );
    }

    void initRegisterResponse() {
        processor.register("register", (a) -> a
                .description("s")
                .subcommand("request", (b) -> b
                        .executes(this::registerMsg))
        );
    }
}
