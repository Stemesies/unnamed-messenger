package elements;

import java.util.ArrayList;

public abstract class AbstractGroup {
    protected int id;
    protected String groupName;
    protected String name;
    protected GroupTypes type;
    protected ArrayList<Integer> members;
    protected ArrayList<Integer> admins;
    protected int owner;
    protected ArrayList<Integer> messages; // id-s сообщений группы

    public int getIdGroup() {
        return this.id;
    }

    public String getGroupName() {
        return groupName;
    }

    public abstract void includeUser(int id);

    public abstract void excludeUser(int id);

}
