package com.healthcare.servlet;

import com.healthcare.util.ApiResponse;
import com.healthcare.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic and verification controller for Review 1.
 * Provides live proof of database connectivity, JDBC connection pooling,
 * schema table row counts, and an end-to-end CRUD round-trip benchmark.
 * <p>
 * Accessible at {@code /db-test} and {@code /admin/db-test}.
 * </p>
 */
@WebServlet(name = "DbTestServlet", urlPatterns = {"/db-test", "/admin/db-test"})
public class DbTestServlet extends BaseServlet {

    public static class CrudStepResult {
        private final String operation;
        private final String query;
        private final boolean success;
        private final long durationMs;
        private final String details;

        public CrudStepResult(String operation, String query, boolean success, long durationMs, String details) {
            this.operation = operation;
            this.query = query;
            this.success = success;
            this.durationMs = durationMs;
            this.details = details;
        }

        public String getOperation() { return operation; }
        public String getQuery() { return query; }
        public boolean isSuccess() { return success; }
        public long getDurationMs() { return durationMs; }
        public String getDetails() { return details; }
    }

    public static class DiagnosticReport {
        private boolean connected;
        private long pingLatencyMs;
        private String databaseProductName;
        private String databaseProductVersion;
        private String driverName;
        private String driverVersion;
        private String catalog;
        private String jdbcUrl;
        private int maxConnections;
        private Map<String, Integer> tableRowCounts = new LinkedHashMap<>();
        private List<CrudStepResult> crudResults = new ArrayList<>();
        private boolean allCrudPassed;
        private long totalCrudDurationMs;
        private String statusMessage;

        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }

        public long getPingLatencyMs() { return pingLatencyMs; }
        public void setPingLatencyMs(long pingLatencyMs) { this.pingLatencyMs = pingLatencyMs; }

        public String getDatabaseProductName() { return databaseProductName; }
        public void setDatabaseProductName(String databaseProductName) { this.databaseProductName = databaseProductName; }

        public String getDatabaseProductVersion() { return databaseProductVersion; }
        public void setDatabaseProductVersion(String databaseProductVersion) { this.databaseProductVersion = databaseProductVersion; }

        public String getDriverName() { return driverName; }
        public void setDriverName(String driverName) { this.driverName = driverName; }

        public String getDriverVersion() { return driverVersion; }
        public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }

        public String getCatalog() { return catalog; }
        public void setCatalog(String catalog) { this.catalog = catalog; }

        public String getJdbcUrl() { return jdbcUrl; }
        public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public Map<String, Integer> getTableRowCounts() { return tableRowCounts; }
        public void setTableRowCounts(Map<String, Integer> tableRowCounts) { this.tableRowCounts = tableRowCounts; }

        public List<CrudStepResult> getCrudResults() { return crudResults; }
        public void setCrudResults(List<CrudStepResult> crudResults) { this.crudResults = crudResults; }

        public boolean isAllCrudPassed() { return allCrudPassed; }
        public void setAllCrudPassed(boolean allCrudPassed) { this.allCrudPassed = allCrudPassed; }

        public long getTotalCrudDurationMs() { return totalCrudDurationMs; }
        public void setTotalCrudDurationMs(long totalCrudDurationMs) { this.totalCrudDurationMs = totalCrudDurationMs; }

        public String getStatusMessage() { return statusMessage; }
        public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DiagnosticReport report = runDiagnostics();

        if (isAjaxRequest(req) || "json".equalsIgnoreCase(req.getParameter("format"))) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Database diagnostic executed successfully", report));
            return;
        }

        req.setAttribute("report", report);
        req.setAttribute("pageTitle", "Database Connectivity & Diagnostic Proof");
        forwardToView(req, resp, "db-test");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Re-run diagnostic on form post
        doGet(req, resp);
    }

    private DiagnosticReport runDiagnostics() {
        DiagnosticReport report = new DiagnosticReport();
        long startPing = System.currentTimeMillis();

        try (Connection conn = DBConnection.getConnection()) {
            report.pingLatencyMs = System.currentTimeMillis() - startPing;
            report.connected = conn.isValid(2);

            DatabaseMetaData meta = conn.getMetaData();
            report.databaseProductName = meta.getDatabaseProductName();
            report.databaseProductVersion = meta.getDatabaseProductVersion();
            report.driverName = meta.getDriverName();
            report.driverVersion = meta.getDriverVersion();
            report.catalog = conn.getCatalog();
            report.jdbcUrl = DBConnection.getJdbcUrl();
            report.maxConnections = meta.getMaxConnections();

            // Table counts
            String[] tables = {
                    "users", "doctors", "patients", "schedules",
                    "time_slots", "appointments", "medical_records",
                    "feedback", "notifications", "system_settings",
                    "audit_logs", "login_attempts"
            };

            for (String tbl : tables) {
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tbl)) {
                    if (rs.next()) {
                        report.tableRowCounts.put(tbl, rs.getInt(1));
                    }
                } catch (Exception e) {
                    logger.warn("Could not count rows for table {}", tbl, e);
                    report.tableRowCounts.put(tbl, -1);
                }
            }

            // Perform sample CRUD round trip on system_settings table
            runSampleCrud(conn, report);

        } catch (Exception e) {
            logger.error("DB Diagnostic failed", e);
            report.connected = false;
            report.statusMessage = "Database connection error: " + e.getMessage();
        }

        return report;
    }

    private void runSampleCrud(Connection conn, DiagnosticReport report) {
        String canaryKey = "__db_test_canary_" + System.currentTimeMillis();
        String initialValue = "Review-1-Initial-Payload";
        String updatedValue = "Review-1-Verified-Updated-Payload";
        String description = "Automated Review 1 connectivity verification canary";

        long totalStart = System.currentTimeMillis();
        boolean allOk = true;

        // 1. CREATE (INSERT)
        long t0 = System.currentTimeMillis();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO system_settings (setting_key, setting_value, description) VALUES (?, ?, ?)")) {
            ps.setString(1, canaryKey);
            ps.setString(2, initialValue);
            ps.setString(3, description);
            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - t0;
            report.crudResults.add(new CrudStepResult(
                    "CREATE",
                    "INSERT INTO system_settings (setting_key, setting_value, description) VALUES (?, ?, ?)",
                    rows == 1,
                    duration,
                    "Inserted 1 canary row with key='" + canaryKey + "'"
            ));
        } catch (Exception e) {
            allOk = false;
            report.crudResults.add(new CrudStepResult("CREATE", "INSERT ...", false, System.currentTimeMillis() - t0, e.getMessage()));
        }

        // 2. READ (SELECT)
        long t1 = System.currentTimeMillis();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT setting_value FROM system_settings WHERE setting_key = ?")) {
            ps.setString(1, canaryKey);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = rs.next();
                String fetched = found ? rs.getString("setting_value") : null;
                boolean match = initialValue.equals(fetched);
                long duration = System.currentTimeMillis() - t1;
                report.crudResults.add(new CrudStepResult(
                        "READ",
                        "SELECT setting_value FROM system_settings WHERE setting_key = ?",
                        match,
                        duration,
                        match ? "Retrieved verified payload: '" + fetched + "'" : "Value mismatch or not found"
                ));
                if (!match) allOk = false;
            }
        } catch (Exception e) {
            allOk = false;
            report.crudResults.add(new CrudStepResult("READ", "SELECT ...", false, System.currentTimeMillis() - t1, e.getMessage()));
        }

        // 3. UPDATE
        long t2 = System.currentTimeMillis();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?")) {
            ps.setString(1, updatedValue);
            ps.setString(2, canaryKey);
            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - t2;
            report.crudResults.add(new CrudStepResult(
                    "UPDATE",
                    "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?",
                    rows == 1,
                    duration,
                    "Updated canary setting_value to: '" + updatedValue + "'"
            ));
            if (rows != 1) allOk = false;
        } catch (Exception e) {
            allOk = false;
            report.crudResults.add(new CrudStepResult("UPDATE", "UPDATE ...", false, System.currentTimeMillis() - t2, e.getMessage()));
        }

        // 4. DELETE
        long t3 = System.currentTimeMillis();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM system_settings WHERE setting_key = ?")) {
            ps.setString(1, canaryKey);
            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - t3;
            report.crudResults.add(new CrudStepResult(
                    "DELETE",
                    "DELETE FROM system_settings WHERE setting_key = ?",
                    rows == 1,
                    duration,
                    "Deleted canary row cleanly (rows affected: " + rows + ")"
            ));
            if (rows != 1) allOk = false;
        } catch (Exception e) {
            allOk = false;
            report.crudResults.add(new CrudStepResult("DELETE", "DELETE ...", false, System.currentTimeMillis() - t3, e.getMessage()));
        }

        report.totalCrudDurationMs = System.currentTimeMillis() - totalStart;
        report.allCrudPassed = allOk;
        report.statusMessage = allOk ? "All 4 CRUD operations executed successfully in " + report.totalCrudDurationMs + " ms."
                                     : "One or more CRUD operations encountered an issue.";
    }
}
