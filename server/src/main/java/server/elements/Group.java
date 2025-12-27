package server.elements;

import server.managers.DatabaseManager;
import utils.Ansi;
import utils.StringPrintWriter;
import utils.elements.AbstractGroup;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Group extends AbstractGroup {

    public Group(int owner, String groupname, String name) {
        this.groupname = groupname;
        this.name = name;
        this.id = groupname.hashCode();
        this.owner = owner;
    }

    @Override
    public void includeUser(int id) {

    }

    @Override
    public void excludeUser(int id) {

    }

    public static boolean groupExists(String groupname) {
        String sql = "SELECT EXISTS(SELECT 1 FROM groups WHERE groupname = ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, groupname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static void addGroup(int ownerId, Group group) {
        String sql = "INSERT INTO groups (groupname, name, type, owner_id)\n"
                + "VALUES (?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, group.groupname);
            stmt.setString(2, group.groupname);
            stmt.setInt(3, 0);
            stmt.setInt(4, ownerId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding Group: " + e.getMessage());
        }
    }

    public static void deleteGroup(StringPrintWriter out, String groupname) {
        String sql = "DELETE FROM groups WHERE id=?; DELETE FROM group_members WHERE group_id=?";

        var group = getGroupByName(groupname);
        if (group == null) {
            out.println("Group not exists or something went wrong.");
            return;
        }
        var members = group.getMembersId();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, group.id);
            stmt.setInt(2, group.id);
            int deletedRows = stmt.executeUpdate();

            if (deletedRows > 0) {
                out.println("Deleted successfully");
            } else {
                System.out.println("Nothing to delete.");
            }

            if (members == null)
                return;


            for (int i : members) {
                var kickedClient = ServerData.findClient(i);
                if (kickedClient == null)
                    continue;

                if (kickedClient.group != null && kickedClient.group.groupname.equals(groupname))
                    kickedClient.sendln(
                        "Group \"" + kickedClient.group.name + "\" was deleted."
                    );
                kickedClient.group = null;
            }


        } catch (SQLException e) {
            System.err.println("Error deleting group: " + e.getMessage());
            out.println("Something went wrong: " + e.getMessage());
        }
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

        if (groupExists(groupname)) {
            out.println(Ansi.Colors.RED.apply("Groupname is already in use."));
            return null;
        }
        Group group = new Group(owner.getId(), groupname, name);
        addGroup(owner.getId(), group);
        group = getGroupByName(groupname);
        addMember(group.getIdGroup(), owner.getUserName(), true);
        out.println("Registered successfully.");
        ServerData.addGroup(group);

        return group;
    }

    public static ArrayList<Integer> getArrayListFromPgArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) return new ArrayList<>();
        Integer[] array = (Integer[]) sqlArray.getArray();
        return new ArrayList<>(Arrays.asList(array));
    }

    public static Group getGroupByName(String groupname) {
        String sql = "SELECT * FROM groups WHERE groupname = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, groupname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int owner = rs.getInt("owner_id");
                String name = rs.getString("groupname");
                Group group = new Group(owner, groupname, name);

                group.id = rs.getInt("id");

                return group;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    public static void addMember(int groupId, String username, boolean isAdmin) {
        String sql = "INSERT INTO group_members (group_id, user_id, is_admin) VALUES (?, ?, ?)";

        int id = User.getUserIdByUsername(username);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, id);
            stmt.setBoolean(3, isAdmin);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
    }

    public static boolean removeMember(StringPrintWriter out, String groupname, String username) {

        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";

        var group = getGroupByName(groupname);
        int id = User.getUserIdByUsername(username);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, group.id);
            stmt.setInt(2, id);
            int deletedRows = stmt.executeUpdate();

            if (deletedRows == 0) {
                out.println(username + " was not a member.");
                return false;
            } else {
                System.out.println("Deleted " + username + " from group " + groupname);
            }

            var kickedClient = ServerData.findClient(username);
            if (kickedClient == null)
                return true;

            if (kickedClient.group != null && kickedClient.group.groupname.equals(groupname))
                kickedClient.sendln(
                    "You were kicked from group \"" + kickedClient.group.name + "\""
                );
            kickedClient.group = null;
            return true;


        } catch (SQLException e) {
            System.err.println("Error removing member: " + e.getMessage());
            out.println("Something went wrong: " + e.getMessage());
            return false;
        }
    }

    public List<Integer> getMembersId() {
        List<Integer> members = new ArrayList<>();
        String sql = "SELECT * FROM group_members WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting Members: " + e.getMessage());
        }

        return members;
    }

    public void invite(StringPrintWriter out, String username, int groupId) {
        if (!User.userExists(username)) {
            out.println(Ansi.Colors.RED.apply("User not found."));
            return;
        }
        addMember(groupId, username, false);
        out.println(username + " invited to " + name);
    }

    public void kick(StringPrintWriter out, String username, String groupname) {
        if (!User.userExists(username)) {
            out.println(Ansi.Colors.RED.apply("User not found."));
            return;
        }
        if (removeMember(out, groupname, username)) {
            out.println(username + " was kicked from " + name);
        }

    }

    public void addMessage(Message message) {
        String sql = "INSERT INTO messages (group_id, content, sender_id)\n"
                + "VALUES (?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            stmt.setString(2, message.getContent());
            stmt.setInt(3, message.getSenderId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding Message: " + e.getMessage());
        }
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM public.messages m JOIN public.users u "
            + "ON m.sender_id = u.id WHERE group_id = ? ORDER BY sent_time ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("name"),
                    rs.getInt("sender_id"),
                    rs.getTimestamp("sent_time")

                ));
            }

        } catch (SQLException e) {
            System.err.println("Error getting Messages: " + e.getMessage());
        }

        return messages;
    }

    public List<Integer> getAdminIds() {
        List<Integer> admins = new ArrayList<>();
        String sql = "SELECT * FROM group_members WHERE group_id = ? AND is_admin = true";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                admins.add(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting Admins: " + e.getMessage());
        }

        return admins;
    }

    public boolean hasAdmin(User user) {
        return getAdminIds().contains(user.getId());
    }

    public boolean isOwner(User user) {
        String sql = "SELECT * FROM groups WHERE groupname = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, groupname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getInt("owner_id") == user.getId())
                    return true;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return false;
    }

    public void loadMemberList() {
        this.members = new ArrayList<>();
        this.admins = new ArrayList<>();

        String sql = "SELECT * FROM group_members gm JOIN users u "
            + "ON gm.user_id=u.id WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                var user = new User(
                    rs.getString("username"),
                    ""
                );
                user.name = rs.getString("name");
                if (rs.getBoolean("is_admin"))
                    admins.add(user);
                else
                    members.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }
}
