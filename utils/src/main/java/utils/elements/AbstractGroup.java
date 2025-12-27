package utils.elements;

import java.util.ArrayList;

public abstract class AbstractGroup {
    protected int id;
    protected String groupname;
    protected String name;
    protected GroupTypes type;
    public ArrayList<AbstractUser> members = new ArrayList<>();
    public ArrayList<AbstractUser> admins = new ArrayList<>();
    protected int owner;

    public int getIdGroup() {
        return this.id;
    }

    public abstract void includeUser(int id);

    public abstract void excludeUser(int id);

    public String getGroupname() {
        return this.groupname;
    }

    public String getName() {
        return this.name;
    }
}
