package client.elements;

import utils.elements.AbstractUser;

public class User extends AbstractUser {

    @Override
    public void sendMessage(String text, int id) {
        // передача данных о сообщении на сервер
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setPassword(String password) {

    }

    @Override
    public void addFriend(int id) {
        this.friends.add(id);
    }
//    @Override
//    public ArrayList<Integer> joinGroup(int id) {
//         отправка запроса на сервер на вступление в группу
//        request.add(id);
//        request.add(this.id); // id пользователя
//        return request;
//    }
}
