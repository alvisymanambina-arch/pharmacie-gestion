package com.pharmacie.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static DatabaseConfig instance;
    private static boolean driverLoaded = false;

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
        loadDriver();
        try {
            Connection connection = DriverManager.getConnection(
                    AppConstants.DB_URL,
                    AppConstants.DB_USER,
                    AppConstants.DB_PASSWORD
            );
            return connection;
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
