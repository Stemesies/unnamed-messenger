package elements;

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
    public ArrayList<Integer> joinGroup(int id) { // ? extends Group
                                                 // id группы, в которую вступает пользоватль
                                                 // возвращает переданный на сервер id пользователя?

        // отправка запроса на сервер на вступление в группу
        request.add(id);
        request.add(this.id); // id пользователя
        return request;
    }

    public int getUserId() {
        return this.id;
    }

    public ArrayList<Integer>  getFriends() {
        return this.friends;
    }

}
