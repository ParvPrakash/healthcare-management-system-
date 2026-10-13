<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Consultation Schedule — CareFirst Doctor" scope="request"/>
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
                        <h1 class="page-title">Patient Consultations & Queue</h1>
                        <p class="page-subtitle">Track confirmed visits, record diagnosis/prescriptions, and mark consultations complete</p>
                    </div>
                </div>

                <!-- Upcoming Consultations Table -->
                <div class="bento-card col-span-12 reveal-card" style="margin-bottom: 32px; --i: 0;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                        <div>
                            <h3 style="font-size: 1.25rem;">Upcoming Consultations</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">${upcomingAppointments.size()} scheduled</p>
                        </div>
                    </div>

                    <div class="table-container">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>Date & Time</th>
                                    <th>Patient Name</th>
                                    <th>Reason for Visit</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Consultation Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty upcomingAppointments}">
                                        <c:forEach var="app" items="${upcomingAppointments}">
                                            <tr>
                                                <td>
                                                    <div style="font-weight: 600;"><c:out value="${app.appointmentDate}" /></div>
                                                    <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${app.formattedTime}" /></div>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 600;"><c:out value="${app.patientName}" /></div>
                                                </td>
                                                <td><c:out value="${app.reasonForVisit}" /></td>
                                                <td>
                                                    <span class="chip ${app.statusBadgeClass}">
                                                        <c:out value="${app.status.displayName}" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 8px;">
                                                        <a href="${pageContext.request.contextPath}/doctor/records?patientId=${app.patientId}&appointmentId=${app.id}" class="btn btn-primary btn-sm">
                                                            EHR & Rx
                                                        </a>

                                                        <!-- Complete Form -->
                                                        <form action="${pageContext.request.contextPath}/appointments/status" method="post" style="display:inline;">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="appointmentId" value="${app.id}">
                                                            <input type="hidden" name="status" value="COMPLETED">
                                                            <button type="submit" class="btn btn-secondary btn-sm" title="Mark consultation completed">
                                                                ✓ Complete
                                                            </button>
                                                        </form>

                                                        <!-- Cancel Form -->
                                                        <form action="${pageContext.request.contextPath}/appointments/status" method="post" style="display:inline;" onsubmit="return confirm('Cancel this consultation?');">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="appointmentId" value="${app.id}">
                                                            <input type="hidden" name="status" value="CANCELLED">
                                                            <button type="submit" class="btn btn-ghost btn-sm" style="color: var(--danger);" title="Cancel">
                                                                ✕
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 32px;">
                                                No upcoming consultations pending in queue.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Past Consultations Table -->
                <div class="bento-card col-span-12 reveal-card" style="--i: 1;">
                    <div style="margin-bottom: 16px;">
                        <h3 style="font-size: 1.25rem;">Past Consultations History</h3>
                        <p style="font-size: 0.85rem; color: var(--text-muted);">Completed and archived patient consultations</p>
                    </div>

                    <div class="table-container">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Patient</th>
                                    <th>Reason</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Record</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty pastAppointments}">
                                        <c:forEach var="app" items="${pastAppointments}">
                                            <tr>
                                                <td><c:out value="${app.appointmentDate}" /> <span style="color: var(--text-muted); font-size: 0.8rem;"><c:out value="${app.formattedTime}" /></span></td>
                                                <td><c:out value="${app.patientName}" /></td>
                                                <td><c:out value="${app.reasonForVisit}" /></td>
                                                <td>
                                                    <span class="chip ${app.statusBadgeClass}">
                                                        <c:out value="${app.status.displayName}" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${pageContext.request.contextPath}/doctor/records?patientId=${app.patientId}" class="btn btn-ghost btn-sm">
                                                        View Patient History &rarr;
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                No past records found.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
