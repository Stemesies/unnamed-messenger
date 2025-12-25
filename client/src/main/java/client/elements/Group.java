package client.elements;

import utils.elements.AbstractGroup;

public class Group extends AbstractGroup {

    public Group() {

    }

    public Group(String name) {
        super.name = name;
    }

    @Override
    public void includeUser(int id) {
//        this.members.add(id); // на сервере находим группу по id и добавляем в неё пользователя
    }

    @Override
    public void excludeUser(int id) {
        this.members.remove(members.indexOf(id));
    }
}
