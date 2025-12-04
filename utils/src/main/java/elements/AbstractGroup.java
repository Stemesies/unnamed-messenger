package elements;

import java.util.ArrayList;

public abstract class AbstractGroup {
    private int id;
    private String groupName;
    private String name;
    private GroupTypes type;
    protected ArrayList<Integer> members;
    private ArrayList<Integer> admins;
    private int owner;
    private ArrayList<Integer> messages; // id-s сообщений группы

    public int getIdGroup() {
        return this.id;
    }

    public abstract void includeUser(int id);
    public abstract void excludeUser(int id);

}
