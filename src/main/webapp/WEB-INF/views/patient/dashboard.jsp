<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="My Health Portal — CareFirst Patient" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
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
                        <h1 class="page-title">Hello, <c:out value="${sessionScope.USER.name}" /> 👋</h1>
                        <p class="page-subtitle">Your personalized clinical care dashboard and appointment scheduler</p>
                    </div>
                    <div class="header-actions">
                        <a href="${pageContext.request.contextPath}/patient/doctors" class="btn btn-primary btn-sm">
                            + Book New Appointment
                        </a>
                    </div>
                </div>

                <!-- Next Upcoming Appointment Banner Card -->
                <c:choose>
                    <c:when test="${not empty nextAppointment}">
                        <div class="bento-card col-span-12 reveal-card" style="background: linear-gradient(135deg, var(--surface), var(--peach-50)); border-color: var(--peach-300); margin-bottom: 28px; --i: 0;">
                            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
                                <div style="display: flex; align-items: center; gap: 20px;">
                                    <div class="brand-icon" style="width: 52px; height: 52px; font-size: 1.5rem;">
                                        🩺
                                    </div>
                                    <div>
                                        <div class="chip status-peach" style="margin-bottom: 6px;">Next Confirmed Consultation</div>
                                        <h2 style="font-size: 1.45rem; margin-bottom: 4px;">Dr. <c:out value="${nextAppointment.doctorName}" /></h2>
                                        <p style="font-size: 0.88rem; color: var(--text-muted);">
                                            <c:out value="${nextAppointment.specialization}" /> • <strong><c:out value="${nextAppointment.appointmentDate}" /> at <c:out value="${nextAppointment.formattedTime}" /></strong>
                                        </p>
                                    </div>
                                </div>
                                <div style="display: flex; gap: 10px;">
                                    <a href="${pageContext.request.contextPath}/patient/appointments" class="btn btn-secondary btn-sm">
                                        View Details
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="bento-card col-span-12 reveal-card" style="background: var(--surface-soft); margin-bottom: 28px; text-align: center; padding: 32px; --i: 0;">
                            <h3 style="margin-bottom: 8px;">No Upcoming Consultations</h3>
                            <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 16px;">Schedule your routine check-up or consult a specialist with ease.</p>
                            <a href="${pageContext.request.contextPath}/patient/doctors" class="btn btn-primary btn-sm">Find a Specialist Doctor &rarr;</a>
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- Bento Grid: Quick Health Stats & Prescriptions -->
                <div class="bento-grid">
                    <div class="bento-card col-span-4 stat-card reveal-card" style="--i: 1;">
                        <div class="stat-header">
                            <span class="stat-title">Total Consultations</span>
                            <div class="stat-icon">📅</div>
                        </div>
                        <div class="stat-value"><span data-counter="${appointments.size()}">0</span></div>
                        <div class="stat-delta positive">Care history entries</div>
                    </div>

                    <div class="bento-card col-span-4 stat-card reveal-card" style="--i: 2;">
                        <div class="stat-header">
                            <span class="stat-title">Medical Records</span>
                            <div class="stat-icon">📋</div>
                        </div>
                        <div class="stat-value"><span data-counter="${recentRecords.size()}">0</span></div>
                        <div class="stat-delta positive">EMR reports filed</div>
                    </div>

                    <div class="bento-card col-span-4 stat-card reveal-card" style="--i: 3;">
                        <div class="stat-header">
                            <span class="stat-title">Profile Status</span>
                            <div class="stat-icon">👤</div>
                        </div>
                        <div class="stat-value" style="font-size: 1.4rem; color: var(--success);">Verified</div>
                        <div class="stat-delta positive"><a href="${pageContext.request.contextPath}/profile" style="color: var(--peach-700);">Edit details &rarr;</a></div>
                    </div>

                    <!-- Recent Appointments Table -->
                    <div class="bento-card col-span-7 reveal-card" style="--i: 4;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                            <h3 style="font-size: 1.15rem;">Recent Bookings</h3>
                            <a href="${pageContext.request.contextPath}/patient/appointments" class="btn btn-ghost btn-sm">All &rarr;</a>
                        </div>

                        <div class="table-container">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Doctor</th>
                                        <th>Status</th>
                                        <th>Fee</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty appointments}">
                                            <c:forEach var="app" items="${appointments}" begin="0" end="3">
                                                <tr>
                                                    <td>
                                                        <div style="font-weight: 600;"><c:out value="${app.appointmentDate}" /></div>
                                                        <div style="font-size: 0.75rem; color: var(--text-muted);"><c:out value="${app.formattedTime}" /></div>
                                                    </td>
                                                    <td><c:out value="${app.doctorName}" /></td>
                                                    <td>
                                                        <span class="chip ${app.statusBadgeClass}">
                                                            <c:out value="${app.status.displayName}" />
                                                        </span>
                                                    </td>
                                                    <td>$<fmt:formatNumber value="${app.consultationFee}" minFractionDigits="0"/></td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    No appointments booked yet.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Recent Prescriptions & Records -->
                    <div class="bento-card col-span-5 reveal-card" style="--i: 5;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                            <h3 style="font-size: 1.15rem;">Latest Medical Records</h3>
                            <a href="${pageContext.request.contextPath}/patient/records" class="btn btn-ghost btn-sm">All &rarr;</a>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 14px;">
                            <c:choose>
                                <c:when test="${not empty recentRecords}">
                                    <c:forEach var="rec" items="${recentRecords}" begin="0" end="2">
                                        <div style="background: var(--surface-soft); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 14px;">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                                                <span style="font-weight: 600; font-size: 0.9rem; color: var(--peach-700);"><c:out value="${rec.diagnosis}" /></span>
                                                <span style="font-size: 0.75rem; color: var(--text-muted);"><c:out value="${rec.recordDate}" /></span>
                                            </div>
                                            <div style="font-size: 0.82rem; margin-bottom: 4px;">
                                                <strong>Doctor:</strong> Dr. <c:out value="${rec.doctorName}" />
                                            </div>
                                            <div style="font-size: 0.8rem; color: var(--text-muted);">
                                                Rx: <c:out value="${rec.prescription}" />
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align: center; color: var(--text-muted); padding: 24px;">
                                        No clinical records entered yet.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
