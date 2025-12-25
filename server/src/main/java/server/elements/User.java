package server.elements;

import server.managers.FriendManager;
import server.managers.JoinManager;
import server.managers.Manager;
import server.managers.RegisterManager;
import utils.Ansi;
import utils.StringPrintWriter;
import utils.elements.AbstractUser;
import utils.elements.SuperRequest;
import utils.extensions.CollectionExt;
import utils.extensions.StringExt;

import java.util.ArrayList;

public class User extends AbstractUser {

    ArrayList<? extends SuperRequest> requests;

    public User(String username, String password) {
        this.userName = username;
        this.name = username;
        this.password = password;
        this.id = username.hashCode();
    }

    public static User register(StringPrintWriter out, String username, String password) {
        if (username.length() > ServerData.MAX_USERNAME_LENGTH) {
            out.println(Ansi.Colors.RED.apply(
                "Username is too long. Max: " + ServerData.MAX_USERNAME_LENGTH
            ));
            return null;
        }

        var list = ServerData.getRegisteredUsers();
        for (var u : list) {
            if (u.getUserName().equals(username)) {
                out.println(Ansi.Colors.RED.apply("Username is already in use."));
                return null;
            }
        }
        var user = new User(username, password);
        out.println("Registered successfully.");
        ServerData.addUser(user);
        return user;
    }

    public static User logIn(StringPrintWriter out, String username, String password) {
        var list = ServerData.getRegisteredUsers();
        for (var u : list) {
            if (u.getUserName().equals(username) && u.getPassword().equals(password)) {
                out.printlnf("Logged in as %s.", u.getName());
                return u;
            }
        }
        out.println(Ansi.Colors.RED.apply("Invalid username or password."));
        return null;
    }

    @Override
    public void sendMessage(String text, int id) {
        // отправляет text второму пользователю id?
    }

    @Override
    public void setName(String name) {
        this.name = name;
        // TODO: обновление базы данных.
    }

    @Override
    public void setPassword(String password) {

    }

    @Override
    public void addFriend(int id) {

    }

    @Deprecated
    public void activateManager() {
        while (!this.requests.isEmpty()) {
            SuperRequest r = requests.getFirst();
            Manager manager;
            switch (r.type) {
                case Register:
                    manager = new RegisterManager();
                    /*что-нибудь про регистрацию*/
                    break;
                case Join:
                    // Достаём пользователя и группу из базы данных по id,
                    // инициализируем поля класса.
                    // Пока вставим заглушки
                    User user = new User("", "");
                    Group group = new Group(user, "", "");
                    manager = new JoinManager(user, group);
                    manager.applyManager();
                    break;
                case Friend:
                    // достаём пользователей из базы данных по id, инициилизируем поля класса
                    // response = true, пока мы не продумали ответ второго пользователя
                    User user1 = new User("", "");
                    User user2 = new User("", "");
                    manager = new FriendManager(user1, user2, true);
                    manager.applyManager();
                    break;
                default: return;
            }
        }
    }

    public void sendFriendRequest(StringPrintWriter out, String username) {
        out.println("Sent request to " + username);
    }

    // Возможно переименование в setPassword
    public void changePassword(StringPrintWriter out, String old, String password, String again) {
        if (!this.getPassword().equals(old)) {
            out.println(Ansi.Colors.RED.apply("Invalid password."));
            return;
        }

        if (!password.equals(again)) {
            out.println(Ansi.Colors.RED.apply("Passwords do not match."));
            return;
        }

        this.password = password;
        out.println("Successfully changed password.");
    }

    @SuppressWarnings("checkstyle:LineLength")
    public String getProfile() {
        var connectedCommand = CollectionExt.findBy(ServerData.getClients(), (it) -> it.user == this);
        var onlineMode = connectedCommand == null ? "" : " • online";

        var boxSize = 50;
        var trimmedName = StringExt.limit(this.name, boxSize - 6 - onlineMode.length());
        var trimmedUsername = StringExt.limit(this.userName, boxSize - 5);

        var headerColor = Ansi.BgColors.fromRgb(34, 55, 75);


        return """
            ┌%s┐
            │%s│
            │ @%s │
            │
            └%s┘
            """.formatted(
                "─".repeat(boxSize - 2),
                headerColor.apply(" ") + Ansi.Modes.BOLD.and(headerColor).apply(trimmedName)
                        + headerColor.apply(onlineMode + " ".repeat(boxSize - 3 - onlineMode.length() - trimmedName.length())),
                this.userName + " ".repeat(boxSize - 5 - trimmedUsername.length()),
                "─".repeat(boxSize - 2)
        );
    }

    // Возможно переименование в setName
    public void changeName(StringPrintWriter out, String nickname) {
        // TODO: смена имени
        setName(nickname);
        out.println("Successfully changed nickname.");
    }

    public void dismissFriendRequest(String username) {
        // TODO: Ну, вы знаете.
    }
}
