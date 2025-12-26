package server.elements;

import server.managers.DatabaseManager;
import utils.Ansi;
import utils.StringPrintWriter;
import utils.elements.AbstractGroup;
import utils.elements.Message;

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
        Group group = new Group(owner.getUserId(), groupname, name);
        addGroup(owner.getUserId(), group);
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

    public List<String> getMessagesContent() {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(rs.getString("content"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting Messages: " + e.getMessage());
        }

        return messages;
    }
}
