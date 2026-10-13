<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="All Appointments — CareFirst Admin" scope="request"/>
<c:set var="activeNav" value="appointments" scope="request"/>
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
                        <h1 class="page-title">Appointment Master Register</h1>
                        <p class="page-subtitle">Centralized oversight of all consultations, confirmations, cancellations, and doctor schedules</p>
                    </div>
                    <div class="header-actions">
                        <span class="chip status-peach">${appointments.size()} On Record</span>
                    </div>
                </div>

                <!-- Filters & Search Toolbar -->
                <div class="filter-bar">
                    <form action="${pageContext.request.contextPath}/admin/appointments" method="get" class="filter-group" style="width: 100%; justify-content: space-between;">
                        <div class="search-input-wrap">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                            <input type="text" name="q" class="form-control" placeholder="Search patient or doctor name..." value="<c:out value='${query}' />">
                        </div>

                        <div class="filter-group">
                            <select name="status" class="form-control" onchange="this.form.submit()" style="width: auto;">
                                <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>Pending</option>
                                <option value="CONFIRMED" ${statusFilter == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                                <option value="COMPLETED" ${statusFilter == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                                <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                            </select>

                            <button type="submit" class="btn btn-secondary btn-sm">Filter</button>
                            <c:if test="${not empty query or statusFilter != 'ALL'}">
                                <a href="${pageContext.request.contextPath}/admin/appointments" class="btn btn-ghost btn-sm">Clear</a>
                            </c:if>
                        </div>
                    </form>
                </div>

                <!-- Appointments Table -->
                <div class="table-container">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Date & Time</th>
                                <th>Patient</th>
                                <th>Doctor / Specialty</th>
                                <th>Status</th>
                                <th>Fee</th>
                                <th style="text-align: right;">Status Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty appointments}">
                                    <c:forEach var="app" items="${appointments}">
                                        <tr>
                                            <td><span style="font-family: monospace; color: var(--text-muted);">#<c:out value="${app.id}" /></span></td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${app.appointmentDate}" /></div>
                                                <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${app.formattedTime}" /></div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${app.patientName}" /></div>
                                                <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${app.reasonForVisit}" /></div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${app.doctorName}" /></div>
                                                <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${app.specialization}" /></div>
                                            </td>
                                            <td>
                                                <span class="chip ${app.statusBadgeClass}">
                                                    <c:out value="${app.status.displayName}" />
                                                </span>
                                            </td>
                                            <td>$<fmt:formatNumber value="${app.consultationFee}" minFractionDigits="0"/></td>
                                            <td style="text-align: right;">
                                                <c:if test="${app.status == 'PENDING' or app.status == 'CONFIRMED'}">
                                                    <form action="${pageContext.request.contextPath}/appointments/status" method="post" style="display:inline;">
                                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                        <input type="hidden" name="appointmentId" value="${app.id}">
                                                        <input type="hidden" name="status" value="CANCELLED">
                                                        <button type="submit" class="btn btn-ghost btn-sm" style="color: var(--danger);" title="Cancel appointment" onclick="return confirm('Cancel this appointment?');">
                                                            Cancel
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 32px;">
                                            No appointments found matching search criteria.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination-wrap">
                            <div>Showing page ${currentPage} of ${totalPages}</div>
                            <div class="pagination-links">
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <a href="${pageContext.request.contextPath}/admin/appointments?page=${p}&q=${query}&status=${statusFilter}"
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
