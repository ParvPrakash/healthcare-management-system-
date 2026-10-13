package com.healthcare.servlet;

import com.healthcare.dao.AuditDao;
import com.healthcare.dao.AuditDaoImpl;
import com.healthcare.model.AuditLog;
import com.healthcare.model.Reportable;
import com.healthcare.util.CsvExporter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Compliance Audit Logging and CSV Security Reports.
 */
@WebServlet(name = "AuditServlet", urlPatterns = {"/admin/audit", "/admin/audit/export"})
public class AuditServlet extends BaseServlet {
    private final AuditDao auditDao = new AuditDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/admin/audit/export".equals(path)) {
            handleExportCsv(req, resp);
            return;
        }

        String query = req.getParameter("q");
        String pageStr = req.getParameter("page");

        int page = 1;
        int pageSize = 20;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr));
            } catch (NumberFormatException ignored) {}
        }

        try {
            int offset = (page - 1) * pageSize;
            List<AuditLog> logs = auditDao.searchLogs(query, pageSize, offset);
            int totalCount = auditDao.countSearchLogs(query);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            req.setAttribute("logs", logs);
            req.setAttribute("query", query != null ? query : "");
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", totalCount);

        } catch (Exception e) {
            logger.error("Error retrieving audit logs", e);
            req.setAttribute("errorMessage", "Error loading audit logs: " + e.getMessage());
        }

        forwardToView(req, resp, "admin/audit");
    }

    private void handleExportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<AuditLog> list = auditDao.findRecent(500);
            List<String> headers = Arrays.asList("ID", "Timestamp", "User Email", "Action", "Details", "IP Address");

            List<Reportable> reportables = list.stream().map(l -> new Reportable() {
                @Override
                public String getReportIdentifier() {
                    return String.valueOf(l.getId());
                }

                @Override
                public String getReportTitle() {
                    return l.getAction();
                }

                @Override
                public Map<String, Object> toReportMap() {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", l.getId());
                    m.put("action", l.getAction());
                    return m;
                }

                @Override
                public String toCsvRow() {
                    return String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            l.getId(),
                            l.getCreatedAt(),
                            l.getUserEmail() != null ? l.getUserEmail() : "SYSTEM",
                            l.getAction(),
                            l.getDetails() != null ? l.getDetails().replace("\"", "\"\"") : "",
                            l.getIpAddress() != null ? l.getIpAddress() : "");
                }
            }).collect(Collectors.toList());

            String csv = CsvExporter.exportToCsv(headers, reportables);

            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"audit_logs_export.csv\"");
            resp.getWriter().write(csv);
            resp.getWriter().flush();

        } catch (Exception e) {
            logger.error("Error exporting audit logs to CSV", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV Export failed");
        }
    }
}
