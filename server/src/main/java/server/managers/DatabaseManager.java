package server.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

public class DatabaseManager {
    public static final String PORT = "5432";
    public static final String USERNAME = "JavaM";
    public static final String PASSWORD = "255";

    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public static void init() {
        // Подключение
        config.setJdbcUrl("jdbc:postgresql://localhost:" + PORT + "/" + USERNAME);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        // Оптимизация
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            ds = new HikariDataSource(config);
        } catch (HikariPool.PoolInitializationException e) {
            DatabaseInstallationTutorial.start();
            System.exit(-1);
            return;
        }

        try {
            initTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

//    private DataSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    private static void initTables() throws SQLException {
        String[] tables = {
                // Таблица пользователей
            """
            CREATE TABLE IF NOT EXISTS users
             (
                 id SERIAL PRIMARY KEY ,
                 username VARCHAR(32) NOT NULL UNIQUE ,
                 name VARCHAR(32) ,
                 password VARCHAR(256) NOT NULL ,
                 salt VARCHAR(200) NOT NULL ,
                 last_online TIMESTAMP DEFAULT CURRENT_TIMESTAMP
             );
               \s""",
                // Таблица друзей пользователей
            """
            CREATE TABLE IF NOT EXISTS user_friends (
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE ,
                friend_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE ,
                friendship_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
                PRIMARY KEY (user_id, friend_id) ,
                CHECK (user_id < friend_id)
            );
              \s""",
                // Таблица групп
            """
            CREATE TABLE IF NOT EXISTS groups
             (
                 id    SERIAL PRIMARY KEY ,
                 groupname  VARCHAR(32) NOT NULL ,
                 name VARCHAR(32) ,
                 type SMALLINT CHECK (type >= 0 AND type <= 3) NOT NULL ,
                 owner_id INTEGER NOT NULL REFERENCES users(id)
             );
               \s""",
                // Таблица участников групп
            """
            CREATE TABLE IF NOT EXISTS group_members (
                 group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE ,
                 user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE ,
                 is_admin BOOLEAN DEFAULT FALSE ,
                 joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
                 PRIMARY KEY (group_id, user_id)
            );
              \s""",
                // Таблица сообщений
            """
            CREATE TABLE IF NOT EXISTS messages
            (
                id SERIAL PRIMARY KEY ,
                group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE ,
                content TEXT ,
                sender_id INTEGER NOT NULL ,
                sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
            );
               \s""",
        };

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : tables) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Таблица уже существует, игнорируем
                    if (!e.getMessage().contains("already exists")) {
                        throw e;
                    }
                }
            }

            System.out.println("Database tables init successfully");

        } catch (SQLException e) {
            System.err.println("Error init tables: " + e.getMessage());
            throw e;
        }
    }
}
