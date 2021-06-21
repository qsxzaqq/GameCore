package cc.i9mc.gamecore.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class BungeeSQLUtil {
    private static BungeeSQLUtil databaseManager;
    private HikariDataSource connectionPool;

    private BungeeSQLUtil() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://yxsj-database/bungee");
            config.setUsername("root");
            config.setPassword("338728243");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            connectionPool = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static BungeeSQLUtil getInstance() {
        if (databaseManager == null) {
            databaseManager = new BungeeSQLUtil();
        }
        return databaseManager;
    }

    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
    }
}
