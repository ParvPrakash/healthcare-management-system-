<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Audit Logs & Compliance — CareFirst Admin" scope="request"/>
<c:set var="activeNav" value="audit" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body>
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <div class="app-container">
        <%@ include file="/WEB-INF/views/fragments/sidebar.jspf" %>

        <div class="main-wrapper">
            <%@ include file="/WEB-INF/views/fragments/topbar.jspf" %>

            <main class="content-body">
                <div class="page-header">
                    <div>
                        <h1 class="page-title">Security & Compliance Audit Trail</h1>
                        <p class="page-subtitle">Immutable access ledger logging authentication, profile updates, and clinical transactions</p>
                    </div>
                    <div class="header-actions">
                        <a href="${pageContext.request.contextPath}/admin/audit/export" class="btn btn-secondary btn-sm">
                            📥 Export Audit CSV
                        </a>
                    </div>
                </div>

                <!-- Filters & Search Toolbar -->
                <div class="filter-bar">
                    <form action="${pageContext.request.contextPath}/admin/audit" method="get" class="filter-group" style="width: 100%; justify-content: space-between;">
                        <div class="search-input-wrap">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                            <input type="text" name="q" class="form-control" placeholder="Search by action, email, or IP..." value="<c:out value='${query}' />">
                        </div>

                        <div class="filter-group">
                            <button type="submit" class="btn btn-secondary btn-sm">Search</button>
                            <c:if test="${not empty query}">
                                <a href="${pageContext.request.contextPath}/admin/audit" class="btn btn-ghost btn-sm">Clear</a>
                            </c:if>
                        </div>
                    </form>
                </div>

                <!-- Audit Logs Table -->
                <div class="table-container">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Actor Email</th>
                                <th>Action Taken</th>
                                <th>Audit Details</th>
                                <th>Client IP</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty logs}">
                                    <c:forEach var="log" items="${logs}">
                                        <tr>
                                            <td>
                                                <div style="font-size: 0.84rem; font-family: monospace; color: var(--text-muted);">
                                                    <c:out value="${log.formattedDate}" />
                                                </div>
                                            </td>
                                            <td>
                                                <span style="font-weight: 600;">
                                                    <c:out value="${log.userEmail != null ? log.userEmail : 'SYSTEM'}" />
                                                </span>
                                            </td>
                                            <td>
                                                <span class="chip status-peach">
                                                    <c:out value="${log.action}" />
                                                </span>
                                            </td>
                                            <td>
                                                <span style="font-size: 0.85rem; color: var(--text-muted);">
                                                    <c:out value="${log.details}" />
                                                </span>
                                            </td>
                                            <td>
                                                <span style="font-family: monospace; font-size: 0.8rem; color: var(--text-faint);">
                                                    <c:out value="${log.ipAddress}" />
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 32px;">
                                            No audit entries found.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination-wrap">
                            <div>Showing page ${currentPage} of ${totalPages} (${totalCount} total audit records)</div>
                            <div class="pagination-links">
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <a href="${pageContext.request.contextPath}/admin/audit?page=${p}&q=${query}"
                                       class="btn btn-sm ${p == currentPage ? 'btn-primary' : 'btn-secondary'}">
                                        ${p}
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </main>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
