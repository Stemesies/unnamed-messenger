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

        public ServerContextData(/*Client client, User user, Group group*/) {
//            this.client = client;
//            this.user = user;
//            this.group = group;
        }
    }

    public static final CustomCommandProcessor<ServerContextData> processor = new CustomCommandProcessor<>();

    ServerContextData data = new ServerContextData(/*clientMain, clientMain.user, clientMain.group*/);

    void registerMsg() {
//        var clientMain = new ClientMain("127.0.0.1", 8080);

        System.out.println("you are logged out! Please, log in or register your account.");

    }

    FriendRequest friendRequestMsg() {
        var request = new FriendRequest();

        System.out.printf("You send a friend request from user %s\n! Are you want to approve it?",
                data.user.getUserName());
        Scanner responseInput = new Scanner(System.in);

        if (responseInput.hasNextLine()) {
            String resp = responseInput.nextLine();

            if (resp.equals("y")) {
                request.requested = true;
                request.isResponsed = true;
            } else if (resp.equals("n")) {
                request.requested = false;
                request.isResponsed = true;
            } else request.isResponsed = false;
        }

        return request;
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
}
