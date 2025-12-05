package elements;

import managers.FriendManager;
import managers.JoinManager;
import managers.RegisterManager;
import utils.Ansi;
import utils.StringPrintWriter;

import java.util.ArrayList;

public class User extends AbstractUser {

    ArrayList<? extends SuperRequest> requests;

    public static User register(StringPrintWriter out, String username, String password) {
        out.println("Not realized.");
        // TODO: регистрация
        return null;
    }

    public static void logIn(StringPrintWriter out, String username, String password) {
        out.println("Not realized.");
        // TODO: вход
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

    public void activateManager() {
        while (!this.requests.isEmpty()) {
            SuperRequest r = requests.getFirst();
//            Manager manager = new Manager();
            switch (r.type) {
                case Register:
                    RegisterManager manager1 = new RegisterManager();
                    /*что-нибудь про регистрацию*/
                    break;
                case Join:
                    // Достаём пользователя и группу из базы данных по id,
                    // инициализируем поля класса.
                    // Пока вставим заглушки
                    User user = new User();
                    Group group = new Group();
                    JoinManager manager2 = new JoinManager(user, group);
                    manager2.applyManager();
                    break;
                case Friend:
                    // достаём пользователей из базы данных по id, инициилизируем поля класса
                    // response = true, пока мы не продумали ответ второго пользователя
                    User user1 = new User();
                    User user2 = new User();
                    FriendManager manager3 = new FriendManager(user1, user2, true);
                    manager3.applyManager();
                    break;
                default: return;
            }
        }
    }

    public void sendFriendRequest(String username) {

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

        // TODO: Изменение пароля
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
