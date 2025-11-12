package elements;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Group {
    private int id;
    private String groupName;
    private String name;
    private GroupTypes type;
//    protected int[] members;
    protected ArrayList<Integer> members;
//    protected LinkedList<Integer> members;

    private int[] admins;
    private int owner;
    private int[] messages; // id-s сообщений группы

    public int getIdGroup() {
        return this.id;
    }

    public abstract void includeUser(int id);
    public abstract void excludeUser(int id);

}
