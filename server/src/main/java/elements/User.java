package elements;

import utils.Ansi;
import utils.StringPrintWriter;

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

//    public void activateManager() {
//        while (!this.requests.isEmpty()) {
//            SuperRequest r = requests.getFirst();
////            Manager manager = new Manager();
//            switch (r.type) {
//                case Register:
//                    RegisterManager manager1 = new RegisterManager();
//                    /*что-нибудь про регистрацию*/
//                    break;
//                case Join:
//                    // Достаём пользователя и группу из базы данных по id,
//                    // инициализируем поля класса.
//                    // Пока вставим заглушки
//                    User user = new User("", "");
//                    Group group = new Group();
//                    JoinManager manager2 = new JoinManager(user, group);
//                    manager2.applyManager();
//                    break;
//                case Friend:
//                    // достаём пользователей из базы данных по id, инициилизируем поля класса
//                    // response = true, пока мы не продумали ответ второго пользователя
//                    User user1 = new User("", "");
//                    User user2 = new User("", "");
//                    FriendManager manager3 = new FriendManager(user1, user2, true);
//                    manager3.applyManager();
//                    break;
//                default: return;
//            }
//        }
//    }

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
