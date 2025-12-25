package utils.elements;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGroup {
    protected int id;
    protected String groupname;
    protected String name;
    protected GroupTypes type;
    protected ArrayList<AbstractUser> members = new ArrayList<>();
    protected ArrayList<AbstractUser> admins = new ArrayList<>();
    protected AbstractUser owner;
    protected ArrayList<Message> messages = new ArrayList<>(); // id-s сообщений группы

    public int getIdGroup() {
        return this.id;
    }

    public abstract void includeUser(int id);

    public abstract void excludeUser(int id);

    public String getGroupname() {
        return this.groupname;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<AbstractUser> getMembers() {
        return members;
    }
}
