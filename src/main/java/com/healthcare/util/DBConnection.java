package com.healthcare.util;

import com.healthcare.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * High-performance JDBC Connection Pool Manager powered by HikariCP.
 * Configured dynamically via db.properties.
 */
public final class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static HikariDataSource dataSource;

    static {
        initDataSource();
    }

    private DBConnection() {}

    private static synchronized void initDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IllegalStateException("db.properties not found in classpath");
                }
                props.load(in);
            }

            String driver = System.getenv("DB_DRIVER");
            if (driver == null || driver.isEmpty()) driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

            String url = System.getenv("DB_URL");
            if (url == null || url.isEmpty()) url = System.getenv("MYSQL_URL");
            if (url == null || url.isEmpty()) url = props.getProperty("db.url");

            String username = System.getenv("DB_USERNAME");
            if (username == null || username.isEmpty()) username = System.getenv("DB_USER");
            if (username == null || username.isEmpty()) username = System.getenv("MYSQL_USER");
            if (username == null || username.isEmpty()) username = props.getProperty("db.username", "root");

            String password = System.getenv("DB_PASSWORD");
            if (password == null) password = System.getenv("MYSQL_PASSWORD");
            if (password == null) password = props.getProperty("db.password", "");

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driver);
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "20")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "5")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "10000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));

            // Performance optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP connection pool initialized successfully: {}", config.getJdbcUrl());
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database pool initialization failed", e);
        }
    }

    public static String getJdbcUrl() {
        if (dataSource != null) return dataSource.getJdbcUrl();
        String url = System.getenv("DB_URL");
        if (url == null || url.isEmpty()) url = System.getenv("MYSQL_URL");
        if (url != null && !url.isEmpty()) return url;
        try {
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) props.load(in);
            }
            return props.getProperty("db.url", "jdbc:mysql://localhost:3306/healthcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        } catch (Exception e) {
            return "jdbc:mysql://localhost:3306/healthcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
    }

    public static String getUsername() {
        if (dataSource != null) return dataSource.getUsername();
        String user = System.getenv("DB_USERNAME");
        if (user == null || user.isEmpty()) user = System.getenv("DB_USER");
        if (user == null || user.isEmpty()) user = System.getenv("MYSQL_USER");
        if (user != null && !user.isEmpty()) return user;
        try {
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) props.load(in);
            }
            return props.getProperty("db.username", "root");
        } catch (Exception e) {
            return "root";
        }
    }

    public static String getPassword() {
        if (dataSource != null) return dataSource.getPassword();
        String pwd = System.getenv("DB_PASSWORD");
        if (pwd == null) pwd = System.getenv("MYSQL_PASSWORD");
        if (pwd != null) return pwd;
        try {
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) props.load(in);
            }
            return props.getProperty("db.password", "");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Obtains a pooled JDBC connection.
     *
     * @return active java.sql.Connection
     * @throws DatabaseException on connection acquisition error
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                initDataSource();
            }
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Error leasing connection from HikariCP pool", e);
            throw new DatabaseException("Could not obtain database connection from pool", e);
        }
    }

    /**
     * Gracefully shuts down connection pool on servlet context destruction.
     */
    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariCP database pool...");
            dataSource.close();
            dataSource = null;
        }
    }

    public static boolean isHealthy() {
        if (dataSource == null || dataSource.isClosed()) return false;
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
