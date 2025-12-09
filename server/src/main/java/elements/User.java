package elements;

import utils.Ansi;
import utils.StringPrintWriter;
import utils.extensions.CollectionExt;
import utils.extensions.StringExt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import managers.DatabaseManager;

public class User extends AbstractUser {

    ArrayList<? extends SuperRequest> requests;

    public User(String username, String password) {
        this.userName = username;
        this.name = username;
        this.password = password;
        this.id = username.hashCode();
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

            stmt.executeQuery();

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

        var user = new User(username, password);
        out.println("Registered successfully.");
        addUser(user);
        ServerData.addUser(user);
        return user;
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
        this.name = name;
        // TODO: обновление базы данных.
    }

    @Override
    public void setPassword(String password) {

    }

//    public void activateManager() {
//        while (!this.requests.isEmpty()) {
//            SuperRequest r = requests.getFirst();
////            Manager manager = new Manager();
//            switch (r.type) {
//                case Register:
//                    RegisterManager manager1 = new RegisterManager();
//                    /*что-нибудь про регистрацию*/
//                    break;
//                case Join:
//                    // Достаём пользователя и группу из базы данных по id,
//                    // инициализируем поля класса.
//                    // Пока вставим заглушки
//                    User user = new User("", "");
//                    Group group = new Group();
//                    JoinManager manager2 = new JoinManager(user, group);
//                    manager2.applyManager();
//                    break;
//                case Friend:
//                    // достаём пользователей из базы данных по id, инициилизируем поля класса
//                    // response = true, пока мы не продумали ответ второго пользователя
//                    User user1 = new User("", "");
//                    User user2 = new User("", "");
//                    FriendManager manager3 = new FriendManager(user1, user2, true);
//                    manager3.applyManager();
//                    break;
//                default: return;
//            }
//        }
//    }

    public void sendFriendRequest(StringPrintWriter out, String username) {
        out.println("Sent request to " + username);
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
        out.println("Successfully changed password.");
    }

    @SuppressWarnings("checkstyle:LineLength")
    public String getProfile() {
        var connectedCommand = CollectionExt.findBy(ServerData.getClients(), (it) -> it.user == this);
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
        // TODO: смена имени
        setName(nickname);
        out.println("Successfully changed nickname.");
    }

    public void dismissFriendRequest(String username) {
        // TODO: Ну, вы знаете.
    }
}
