package elements;

import java.util.ArrayList;

public class UserClient extends User{

    public ArrayList<Integer> request;
    @Override
    public void sendMessage(String text) {
        // передача данных о сообщении на сервер
    }

    @Override
    public ArrayList<Integer> joinGroup(int id) {
        // отправка запроса на сервер на вступление в группу
        request.add(id);
        request.add(this.id); // id пользователя
        return request;

    }
}
