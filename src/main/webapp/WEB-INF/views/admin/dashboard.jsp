<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Admin Command Center — CareFirst" scope="request"/>
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
                        <h1 class="page-title">Executive Dashboard</h1>
                        <p class="page-subtitle">Real-time clinical throughput, capacity monitoring, and system activity</p>
                    </div>
                    <div class="header-actions">
                        <span class="chip status-success">
                            ● <c:out value="${liveSessions != null ? liveSessions : 1}" /> Active Session<c:if test="${liveSessions > 1}">s</c:if>
                        </span>
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-primary btn-sm">
                            + Add Doctor / Staff
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/analytics" class="btn btn-secondary btn-sm">
                            Deep Analytics &rarr;
                        </a>
                    </div>
                </div>

                <!-- Bento KPI Cards with Animated Counters -->
                <div class="bento-grid">
                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 0;">
                        <div class="stat-header">
                            <span class="stat-title">Total Patients</span>
                            <div class="stat-icon">👥</div>
                        </div>
                        <div class="stat-value"><span data-counter="${stats.totalPatients}">0</span></div>
                        <div class="stat-delta positive">Registered active patients</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 1;">
                        <div class="stat-header">
                            <span class="stat-title">Consulting Doctors</span>
                            <div class="stat-icon">🩺</div>
                        </div>
                        <div class="stat-value"><span data-counter="${stats.totalDoctors}">0</span></div>
                        <div class="stat-delta positive">Specialists on duty</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 2;">
                        <div class="stat-header">
                            <span class="stat-title">Total Bookings</span>
                            <div class="stat-icon">📅</div>
                        </div>
                        <div class="stat-value"><span data-counter="${stats.totalAppointments}">0</span></div>
                        <div class="stat-delta positive"><c:out value="${bookingsToday != null ? bookingsToday : 0}" /> today</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 3;">
                        <div class="stat-header">
                            <span class="stat-title">Gross Revenue</span>
                            <div class="stat-icon">💳</div>
                        </div>
                        <div class="stat-value">$<span data-counter="${stats.totalRevenue}">0</span></div>
                        <div class="stat-delta positive">Completed consultations</div>
                    </div>

                    <!-- Chart 1: Appointments Velocity (Area Chart) -->
                    <div class="bento-card col-span-8 reveal-card" style="--i: 4;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                            <div>
                                <h3 style="font-size: 1.15rem;">Appointment Trends (Last 7 Days)</h3>
                                <p style="font-size: 0.82rem; color: var(--text-muted);">Daily scheduled patient bookings</p>
                            </div>
                            <span class="chip status-peach">Live Feed</span>
                        </div>
                        <div style="height: 260px; position: relative;">
                            <canvas id="appointmentsPerDayChart"></canvas>
                        </div>
                    </div>

                    <!-- Chart 2: Status Breakdown (Doughnut) -->
                    <div class="bento-card col-span-4 reveal-card" style="--i: 5;">
                        <div style="margin-bottom: 16px;">
                            <h3 style="font-size: 1.15rem;">Consultation Status</h3>
                            <p style="font-size: 0.82rem; color: var(--text-muted);">Current pipeline distribution</p>
                        </div>
                        <div style="height: 260px; position: relative; display: flex; align-items: center; justify-content: center;">
                            <canvas id="statusDoughnutChart"></canvas>
                        </div>
                    </div>

                    <!-- Today's Appointments Table -->
                    <div class="bento-card col-span-8 reveal-card" style="--i: 6;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                            <div>
                                <h3 style="font-size: 1.15rem;">Today's Schedule</h3>
                                <p style="font-size: 0.82rem; color: var(--text-muted);">Appointments scheduled for today</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/admin/appointments" class="btn btn-ghost btn-sm">View All &rarr;</a>
                        </div>

                        <div class="table-container">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>Time</th>
                                        <th>Patient</th>
                                        <th>Doctor</th>
                                        <th>Status</th>
                                        <th>Fee</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty todayAppointments}">
                                            <c:forEach var="app" items="${todayAppointments}">
                                                <tr>
                                                    <td><strong><c:out value="${app.formattedTime}" /></strong></td>
                                                    <td><c:out value="${app.patientName}" /></td>
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
                                                <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    No appointments scheduled for today yet.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Recent Audit & Security Stream -->
                    <div class="bento-card col-span-4 reveal-card" style="--i: 7;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                            <div>
                                <h3 style="font-size: 1.15rem;">Audit Trail</h3>
                                <p style="font-size: 0.82rem; color: var(--text-muted);">Recent security actions</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/admin/audit" class="btn btn-ghost btn-sm">Logs &rarr;</a>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 14px;">
                            <c:forEach var="log" items="${recentLogs}">
                                <div style="display: flex; gap: 12px; align-items: flex-start; padding-bottom: 12px; border-bottom: 1px solid var(--border);">
                                    <div style="width: 8px; height: 8px; border-radius: 50%; background: var(--peach-500); margin-top: 6px; flex-shrink: 0;"></div>
                                    <div style="flex: 1; min-width: 0;">
                                        <div style="font-size: 0.86rem; font-weight: 600; color: var(--text);"><c:out value="${log.action}" /></div>
                                        <div style="font-size: 0.78rem; color: var(--text-muted); text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">
                                            <c:out value="${log.userEmail != null ? log.userEmail : 'SYSTEM'}" /> • <c:out value="${log.ipAddress}" />
                                        </div>
                                    </div>
                                    <div style="font-size: 0.72rem; color: var(--text-faint); white-space: nowrap;">
                                        <c:out value="${log.formattedDate}" />
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <!-- Script holding Chart.js dataset JSON for app.js -->
    <script id="chartDataScript" type="application/json">
        ${chartData}
    </script>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
