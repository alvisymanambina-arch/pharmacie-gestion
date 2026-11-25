package com.pharmacie.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AppConstants {
    public static final String APP_NAME = "PharmaGestion";
    public static final String APP_VERSION = "2.0.0";

    // Database
    // Never commit real credentials. Configure PHARMACIE_DB_URL, PHARMACIE_DB_USER and
    // PHARMACIE_DB_PASSWORD in the operating system (or Java -D properties).
    private static final Map<String, String> DOTENV = loadDotEnv();

    public static final String DB_URL = getConfig("PHARMACIE_DB_URL", "pharmacie.db.url",
            "jdbc:postgresql://HOST:PORT/postgres?sslmode=require");
    public static final String DB_USER = getConfig("PHARMACIE_DB_USER", "pharmacie.db.user", "postgres");
    public static final String DB_PASSWORD = getConfig("PHARMACIE_DB_PASSWORD", "pharmacie.db.password", "");

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_PHARMACIEN = "PHARMACIEN";

    // Themes
    public static final String THEME_LIGHT = "FlatLaf Light";
    public static final String THEME_DARK = "FlatLaf Dark";
    public static final String THEME_INTELLIJ = "Arc Dark (Material)";

    private static String getConfig(String environmentName, String propertyName, String defaultValue) {
        String property = System.getProperty(propertyName);
        if (property != null && !property.isBlank()) return property;
        String environment = System.getenv(environmentName);
        if (environment != null && !environment.isBlank()) return environment;
        String dotenv = DOTENV.get(environmentName);
        return dotenv != null && !dotenv.isBlank() ? dotenv : defaultValue;
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path envFile = Path.of(".env");
        if (!Files.isRegularFile(envFile)) return values;
        try {
            for (String line : Files.readAllLines(envFile)) {
                String entry = line.trim();
                if (entry.isEmpty() || entry.startsWith("#")) continue;
                if (entry.startsWith("export ")) entry = entry.substring(7).trim();
                int separator = entry.indexOf('=');
                if (separator <= 0) continue;
                String key = entry.substring(0, separator).trim();
                String value = entry.substring(separator + 1).trim();
                if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("[AppConstants] Impossible de lire le fichier .env : " + e.getMessage());
        }
        return values;
    }

    private AppConstants() {}
}
