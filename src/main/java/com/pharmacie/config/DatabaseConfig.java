package com.pharmacie.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {

    private static DatabaseConfig instance;
    private static boolean driverLoaded = false;
    private volatile HikariDataSource dataSource;

    private DatabaseConfig() {
        loadDriver();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadDriver() {
        if (!driverLoaded) {
            try {
                Class.forName("org.postgresql.Driver");
                driverLoaded = true;
                System.out.println("[DatabaseConfig] PostgreSQL JDBC driver loaded successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("[DatabaseConfig] PostgreSQL JDBC Driver not found: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private HikariDataSource getDataSource() {
        HikariDataSource current = dataSource;
        if (current == null) {
            synchronized (this) {
                current = dataSource;
                if (current == null) {
                    loadDriver();
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(AppConstants.DB_URL);
                    config.setUsername(AppConstants.DB_USER);
                    config.setPassword(AppConstants.DB_PASSWORD);
                    config.setPoolName("PharmaGestion-Supabase");
                    config.setMaximumPoolSize(4);
                    config.setMinimumIdle(1);
                    config.setConnectionTimeout(10_000);
                    config.setValidationTimeout(5_000);
                    config.setIdleTimeout(300_000);
                    config.setMaxLifetime(1_500_000);
                    config.addDataSourceProperty("tcpKeepAlive", "true");
                    String localTimeZone = ZoneId.systemDefault().getId().replace("'", "''");
                    config.setConnectionInitSql("SET TIME ZONE '" + localTimeZone + "'");
                    // Let the pool initialize in the background so the first screen stays responsive.
                    config.setInitializationFailTimeout(-1);
                    current = new HikariDataSource(config);
                    dataSource = current;
                }
            }
        }
        return current;
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("[DatabaseConfig] Connection test successful.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DatabaseConfig] Connection closed successfully.");
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseConfig] Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
