package server.elements;

import utils.Ansi;
import utils.StringPrintWriter;
import utils.extensions.CollectionExt;
import utils.extensions.StringExt;
import utils.elements.AbstractUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.managers.DatabaseManager;

public class User extends AbstractUser {

    public User(String username, String password) {
        this.userName = username;
        this.name = username;
        this.password = password;
        this.id = username.hashCode();
    }

    public static User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString(2);
                String password = rs.getString(4);
                User user = new User(username, password);
                user.name = rs.getString(3);
                user.id = rs.getInt(1);

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String password = rs.getString("password");
                User user = new User(username, password);
                user.name = rs.getString("name");
                user.id = rs.getInt("id");

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    public static Integer getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    public static boolean userExists(String username) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static String getSalt() {
        // TODO: добавить создание соли
        return "1";
    }

    public static String getHash(String password, String salt) {
        // TODO: добавить получение хеша
        return password;
    }

    public static void addUser(User user) {
        String sql = "INSERT INTO users (username, name, password, salt)\n"
                + "VALUES (?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.userName);
            stmt.setString(2, user.userName);

            String salt = getSalt();
            String password = getHash(user.password, salt);

            stmt.setString(3, password);
            stmt.setString(4, salt);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding User: " + e.getMessage());
        }
    }

    public static User register(StringPrintWriter out, String username, String password) {
        if (username.length() > ServerData.MAX_USERNAME_LENGTH) {
            out.println(Ansi.Colors.RED.apply(
                "Username is too long. Max: " + ServerData.MAX_USERNAME_LENGTH
            ));
            return null;
        }

        if (userExists(username)) {
            out.println(Ansi.Colors.RED.apply("Username is already in use."));
            return null;
        }

        addUser(new User(username, password));
        out.println("Registered successfully.");

        return getUserByUsername(username);
    }

    public static void updateLastOnline(String username) {
        String sql = "UPDATE users SET last_online = CURRENT_TIMESTAMP WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating User`s last online: " + e.getMessage());
        }
    }

    public static User logIn(StringPrintWriter out, String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String inputPassword = getHash(password, salt);

                if (storedPassword.equals(inputPassword)) {
                    User user = new User(username, password);
                    user.name = rs.getString("name");
                    user.id = rs.getInt("id");

                    // Обновляем время последнего входа
                    updateLastOnline(user.name);

                    out.printlnf("Logged in as %s.", user.name);
                    return user;
                } else {
                    out.println(Ansi.Colors.RED.apply("Invalid password."));
                }
            } else {
                out.println(Ansi.Colors.RED.apply("User not found."));
            }

        } catch (SQLException e) {
            out.println(Ansi.Colors.RED.apply("Login error: " + e.getMessage()));
        }

        return null;
    }

    @Override
    public void sendMessage(String text, int id) {
        // отправляет text второму пользователю id?
    }

    @Override
    public void setName(String name) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, this.id);
            stmt.executeUpdate();
            this.name = name;

        } catch (SQLException e) {
            System.err.println("Error updating User`s name: " + e.getMessage());
        }
    }

    @Override
    public void setPassword(String password) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, password);
            stmt.setInt(2, this.id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating User`s password: " + e.getMessage());
        }
    }

    @Override
    public void addFriend(int id) {

    }

    public void addFriend(StringPrintWriter out, String username) {
        Integer friendId = getUserIdByUsername(username);
        if (friendId == null) {
            out.println(Ansi.Colors.RED.apply("User not found."));
            return;
        }

        String sql = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int user1 = Math.min(this.id, friendId);
            int user2 = Math.max(this.id, friendId);
            stmt.setInt(1, user1);
            stmt.setInt(2, user2);
            stmt.executeUpdate();

            out.println("You are now friends with " + username);

        } catch (SQLException e) {
            System.err.println("Error adding User`s friend: " + e.getMessage());
        }
    }

    public void sendFriendRequest(StringPrintWriter out, String username) {
        out.println("Sent request to " + username);
        addFriend(out, username);
    }

    public List<Integer> getFriendsId() {
        List<Integer> friends = new ArrayList<>();
        String sql = """
               SELECT u.id FROM users u
               JOIN user_friends uf ON (u.id = uf.friend_id AND uf.user_id = ?)
                                           OR (u.id = uf.user_id AND uf.friend_id = ?)
               WHERE u.id != ?""";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            stmt.setInt(2, this.id);
            stmt.setInt(3, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                friends.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting Friends: " + e.getMessage());
        }

        return friends;
    }

    // Возможно переименование в setPassword
    public void changePassword(StringPrintWriter out, String old, String password, String again) {
        if (!this.getPassword().equals(old)) {
            out.println(Ansi.Colors.RED.apply("Invalid password."));
            return;
        }

        if (!password.equals(again)) {
            out.println(Ansi.Colors.RED.apply("Passwords do not match."));
            return;
        }

        this.password = password;
        setPassword(password);
        out.println("Successfully changed password.");
    }

    @SuppressWarnings("checkstyle:LineLength")
    public String getProfile() {
        // FIXME
        var connectedCommand = CollectionExt.findBy(
            ServerData.getRegisteredClients(), (it) -> it.user == this
        );

        var onlineMode = connectedCommand == null ? "" : " • online";

        var boxSize = 50;
        var trimmedName = StringExt.limit(this.name, boxSize - 6 - onlineMode.length());
        var trimmedUsername = StringExt.limit(this.userName, boxSize - 5);

        var headerColor = Ansi.BgColors.fromRgb(34, 55, 75);


        return """
            ┌%s┐
            │%s│
            │ @%s │
            │
            └%s┘
            """.formatted(
                "─".repeat(boxSize - 2),
                headerColor.apply(" ") + Ansi.Modes.BOLD.and(headerColor).apply(trimmedName)
                        + headerColor.apply(onlineMode + " ".repeat(boxSize - 3 - onlineMode.length() - trimmedName.length())),
                this.userName + " ".repeat(boxSize - 5 - trimmedUsername.length()),
                "─".repeat(boxSize - 2)
        );
    }

    // Возможно переименование в setName
    public void changeName(StringPrintWriter out, String nickname) {
        setName(nickname);
        out.println("Successfully changed nickname.");
    }

    public void dismissFriendRequest(String username) {
        // TODO: Ну, вы знаете.
    }
}
