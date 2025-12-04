package elements;

import managers.FriendManager;
import managers.JoinManager;
import managers.RegisterManager;

import java.util.ArrayList;

public class User extends AbstractUser {

    ArrayList<? extends SuperRequest> requests;

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
}
