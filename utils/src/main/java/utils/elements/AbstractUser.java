package utils.elements;

import java.util.ArrayList;

public abstract class AbstractUser {
    protected int id;
    protected String userName;
    protected String name;
    protected String password;
    protected ArrayList<Integer> friends; // массив id'шников друзей

    public ArrayList<Integer> request;

    public abstract void sendMessage(String text, int id);

    /*Реализация этого запроса будет переписана*/
    public ArrayList<Integer> joinGroup(int id) {
        // ? Extends Group
        // id группы, в которую вступает пользователь.
        // Возвращает переданный на сервер id пользователя?

        // отправка запроса на сервер на вступление в группу
        request.add(id);
        request.add(this.id); // id пользователя
        return request;
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<Integer> getFriends() {
        return this.friends;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public abstract void setName(String name);

    public abstract void setPassword(String password);

    public abstract void addFriend(int id);

}
