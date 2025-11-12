package elements;

import java.util.ArrayList;

public abstract class User {
    protected int id; // int - ?
    private String userName;
    private String name;
    private String password;
    private int[] friends; // массив id'шников друзей

    public abstract void sendMessage(String text);
    public abstract ArrayList<Integer> joinGroup(int e); // ? extends Group
                                            // id группы, в которую вступает пользоватль
                                            // возвращает переданный на сервер id пользователя?

}
