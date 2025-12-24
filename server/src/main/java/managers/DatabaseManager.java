package managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {
    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

//    static {
//        init();
//    }

    public static void init() {
        // Подключение
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/JavaM");
        config.setUsername("JavaM");
        config.setPassword("255");

        // Оптимизация
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);

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
                 friends INTEGER[] ,
                 last_online TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
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
                 owner_id INTEGER NOT NULL REFERENCES users(id) ,
                 messages INTEGER[]
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
