package elements;


public class GroupClient extends Group {

    @Override
    public void includeUser(int id) {
        this.members.add(id); // на сервере находим группу по id и добавляем в неё пользователя
    }

    @Override
    public void excludeUser(int id) {
//        Integer test = 5;
        this.members.remove(members.indexOf(id));
    }


}
