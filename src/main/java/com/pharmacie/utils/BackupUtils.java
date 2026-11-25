package com.pharmacie.utils;

import com.pharmacie.config.AppConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupUtils {

    private BackupUtils() {
    }

    public static boolean exportDatabase(String filePath) {
        try {
            PgConnection db = parseConnection();
            List<String> command = db.baseCommand("pg_dump");
            command.add("--no-owner");
            command.add("--no-privileges");
            command.add("--clean");
            command.add("--if-exists");
            command.add("--format=plain");
            command.add("--file");
            command.add(filePath);
            command.add("--dbname");
            command.add(db.database);
            return run(command, db);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[BackupUtils] Erreur lors de l'export : " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[BackupUtils] Export interrompu.");
            return false;
        }
    }

    public static boolean importDatabase(String filePath) {
        try {
            PgConnection db = parseConnection();
            List<String> command = db.baseCommand("psql");
            command.add("--set=ON_ERROR_STOP=1");
            command.add("--single-transaction");
            command.add("--file");
            command.add(filePath);
            command.add("--dbname");
            command.add(db.database);
            return run(command, db);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[BackupUtils] Erreur lors de l'import : " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[BackupUtils] Import interrompu.");
            return false;
        }
    }

    private static boolean run(List<String> command, PgConnection db)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().put("PGPASSWORD", AppConstants.DB_PASSWORD);
        builder.environment().put("PGSSLMODE", db.sslMode);
        builder.environment().put("PGCONNECT_TIMEOUT", "15");
        Process process = builder.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        return process.waitFor() == 0;
    }

    private static PgConnection parseConnection() {
        String jdbcUrl = AppConstants.DB_URL;
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("PHARMACIE_DB_URL doit être une URL JDBC PostgreSQL.");
        }
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String path = uri.getPath();
        String database = path == null || path.length() < 2 ? "postgres" : path.substring(1);
        String sslMode = "require";
        String query = uri.getRawQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                String[] pair = parameter.split("=", 2);
                if (pair.length == 2 && pair[0].equalsIgnoreCase("sslmode")) {
                    sslMode = pair[1];
                }
            }
        }
        return new PgConnection(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 5432,
                database, AppConstants.DB_USER, sslMode);
    }

    private record PgConnection(String host, int port, String database, String user, String sslMode) {
        private List<String> baseCommand(String executable) {
            List<String> command = new ArrayList<>();
            command.add(executable);
            command.add("--host");
            command.add(host);
            command.add("--port");
            command.add(Integer.toString(port));
            command.add("--username");
            command.add(user);
            command.add("--no-password");
            return command;
        }
    }

    public static String generateBackupFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT);
        return "pharmacie_backup_" + sdf.format(new Date()) + ".sql";
    }
}
