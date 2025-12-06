package elements;

import utils.Ansi;
import utils.StringPrintWriter;
import utils.extensions.CollectionExt;

public class Group extends AbstractGroup {

    public Group(User owner, String groupname, String name) {
        this.groupname = groupname;
        this.name = name;
        this.id = groupname.hashCode();
        this.owner = owner;
        this.admins.add(owner);
        this.members.add(owner);
    }

    @Override
    public void includeUser(int id) {

    }

    @Override
    public void excludeUser(int id) {

    }

    public static Group register(
        StringPrintWriter out, User owner, String groupname, String name
    ) {
        if (groupname.length() > ServerData.MAX_USERNAME_LENGTH) {
            out.println(Ansi.Colors.RED.apply(
                "Groupname is too long. Max: " + ServerData.MAX_USERNAME_LENGTH
            ));
            return null;
        }

        var list = ServerData.getRegisteredGroups();
        for (var g : list) {
            if (g.getGroupname().equals(groupname)) {
                out.println(Ansi.Colors.RED.apply("Groupname is already in use."));
                return null;
            }
        }
        Group group = new Group(owner, groupname, name);
        ServerData.addGroup(group);

        return group;
    }

    public void invite(StringPrintWriter out, String username) {
        var user = CollectionExt.findBy(
            ServerData.getRegisteredUsers(),
            (it) -> it.userName.equals(username)
        );
        if (user == null) {
            out.println(Ansi.Colors.RED.apply("User not found."));
            return;
        }
        members.add(user);
        out.println(username + "invited to " + name);
    }
}
