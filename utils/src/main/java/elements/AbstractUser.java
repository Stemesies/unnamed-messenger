package elements;

import java.util.ArrayList;

public abstract class AbstractUser {
    private int id;
    private String userName;
    private String name;
    private String password;
    private ArrayList<Integer> friends; // массив id'шников друзей

    public ArrayList<Integer> request;

    public abstract void sendMessage(String text, int id);
    /*Реализация этого запроса будет переписана*/
    public ArrayList<Integer> joinGroup(int e) { // ? extends Group
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
