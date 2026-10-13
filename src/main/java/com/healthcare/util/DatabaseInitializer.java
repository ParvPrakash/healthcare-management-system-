package com.healthcare.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automates idempotent database bootstrap, table migrations, and demo data seeding.
 * Supports zero-config startup on any fresh MySQL installation.
 */
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static synchronized void initialize() {
        try {
            ensureDatabaseExists();
            ensureTablesAndSeedData();
        } catch (Exception e) {
            logger.error("Database initialization encountered an error: {}", e.getMessage(), e);
        }
    }

    private static void ensureDatabaseExists() {
        String url = DBConnection.getJdbcUrl();
        String username = DBConnection.getUsername();
        String password = DBConnection.getPassword();

        // Extract database name from URL (e.g., jdbc:mysql://localhost:3306/healthcare_db?...)
        String dbName = "healthcare_db";
        try {
            int qIdx = url.indexOf('?');
            String cleanUrl = qIdx > 0 ? url.substring(0, qIdx) : url;
            int lastSlash = cleanUrl.lastIndexOf('/');
            if (lastSlash > 0 && lastSlash < cleanUrl.length() - 1) {
                dbName = cleanUrl.substring(lastSlash + 1);
            }

            // Connect to server root without specifying database name
            int hostEnd = cleanUrl.lastIndexOf('/');
            String rootUrl = cleanUrl.substring(0, hostEnd + 1);
            if (qIdx > 0) {
                rootUrl += url.substring(qIdx);
            }

            logger.info("Checking existence of database '{}' on MySQL server...", dbName);
            try (Connection conn = DriverManager.getConnection(rootUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                logger.info("Database '{}' is verified/ready.", dbName);
            }
        } catch (Exception e) {
            logger.warn("Could not check/create database root (might already exist or restricted permissions): {}", e.getMessage());
        }
    }

    private static void ensureTablesAndSeedData() {
        try (Connection conn = DBConnection.getConnection()) {
            boolean usersTableExists = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'users'")) {
                if (rs.next()) {
                    usersTableExists = true;
                }
            }

            if (!usersTableExists) {
                logger.info("Database tables not found. Executing schema.sql...");
                executeSqlScript(conn, "sql/schema.sql");
                logger.info("Database schema created successfully.");
            } else {
                logger.info("Database schema is already present.");
            }

            // Check if demo users exist
            boolean hasUsers = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasUsers = true;
                }
            }

            if (!hasUsers) {
                logger.info("No user records found. Executing seed.sql to populate demo data...");
                executeSqlScript(conn, "sql/seed.sql");
                logger.info("Demo data seeded successfully.");
            } else {
                logger.info("Demo user data already present in database.");
            }

        } catch (Exception e) {
            logger.error("Failed to verify or seed database tables: {}", e.getMessage(), e);
        }
    }

    public static synchronized void resetDemoData() throws Exception {
        logger.info("Resetting database to default demo state...");
        try (Connection conn = DBConnection.getConnection()) {
            executeSqlScript(conn, "sql/schema.sql");
            executeSqlScript(conn, "sql/seed.sql");
            logger.info("Database successfully reset to demo state.");
        }
    }

    public static void executeSqlScript(Connection conn, String scriptPath) throws Exception {
        List<String> statements = loadSqlStatements(scriptPath);
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        logger.warn("SQL statement warning/error during script execution ({}): {}", trimmed, e.getMessage());
                    }
                }
            }
        }
    }

    private static List<String> loadSqlStatements(String scriptPath) throws Exception {
        List<String> lines = new ArrayList<>();

        // Try ClassLoader first
        InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream(scriptPath);
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } else {
            // Fallback to filesystem path
            Path path = Paths.get(scriptPath);
            if (!Files.exists(path)) {
                path = Paths.get("sql", scriptPath.replace("sql/", ""));
            }
            if (Files.exists(path)) {
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } else {
                throw new IllegalStateException("SQL script file not found: " + scriptPath);
            }
        }

        List<String> statements = new ArrayList<>();
        StringBuilder currentStmt = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.startsWith("--") || line.startsWith("//") || line.startsWith("/*") || line.isEmpty()) {
                continue;
            }

            currentStmt.append(rawLine).append("\n");
            if (line.endsWith(";")) {
                String stmtStr = currentStmt.toString().trim();
                if (stmtStr.endsWith(";")) {
                    stmtStr = stmtStr.substring(0, stmtStr.length() - 1);
                }
                if (!stmtStr.trim().isEmpty()) {
                    statements.add(stmtStr);
                }
                currentStmt.setLength(0);
            }
        }

        if (currentStmt.length() > 0 && !currentStmt.toString().trim().isEmpty()) {
            statements.add(currentStmt.toString().trim());
        }

        return statements;
    }
}
