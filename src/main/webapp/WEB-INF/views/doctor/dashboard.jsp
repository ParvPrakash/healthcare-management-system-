<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Doctor Clinical Portal — CareFirst" scope="request"/>
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
                        <h1 class="page-title">Welcome back, Dr. <c:out value="${sessionScope.USER.name}" /></h1>
                        <p class="page-subtitle">Here is your clinical workload, today's schedule, and patient ratings</p>
                    </div>
                    <div class="header-actions">
                        <a href="${pageContext.request.contextPath}/doctor/schedule" class="btn btn-secondary btn-sm">
                            Manage Availability &rarr;
                        </a>
                        <a href="${pageContext.request.contextPath}/doctor/records" class="btn btn-primary btn-sm">
                            + Write Patient Record
                        </a>
                    </div>
                </div>

                <!-- Bento KPI Cards -->
                <div class="bento-grid">
                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 0;">
                        <div class="stat-header">
                            <span class="stat-title">Patients Today</span>
                            <div class="stat-icon">📅</div>
                        </div>
                        <div class="stat-value"><span data-counter="${todayAppointments.size()}">0</span></div>
                        <div class="stat-delta positive">Scheduled consultations</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 1;">
                        <div class="stat-header">
                            <span class="stat-title">Upcoming Visits</span>
                            <div class="stat-icon">⏳</div>
                        </div>
                        <div class="stat-value"><span data-counter="${upcomingAppointments.size()}">0</span></div>
                        <div class="stat-delta positive">Queue waiting</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 2;">
                        <div class="stat-header">
                            <span class="stat-title">Average Rating</span>
                            <div class="stat-icon">★</div>
                        </div>
                        <div class="stat-value" style="color: var(--peach-700);"><span data-counter="${avgRating}">0.0</span> / 5.0</div>
                        <div class="stat-delta positive">Based on ${totalReviews} reviews</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 3;">
                        <div class="stat-header">
                            <span class="stat-title">Feedback Count</span>
                            <div class="stat-icon">💬</div>
                        </div>
                        <div class="stat-value"><span data-counter="${totalReviews}">0</span></div>
                        <div class="stat-delta positive">Verified patient reviews</div>
                    </div>

                    <!-- Today's Consultations List -->
                    <div class="bento-card col-span-8 reveal-card" style="--i: 4;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                            <div>
                                <h3 style="font-size: 1.15rem;">Today's Patient Queue</h3>
                                <p style="font-size: 0.82rem; color: var(--text-muted);">Consultations for today</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/doctor/appointments" class="btn btn-ghost btn-sm">Full Queue &rarr;</a>
                        </div>

                        <div class="table-container">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>Time</th>
                                        <th>Patient</th>
                                        <th>Reason / Symptoms</th>
                                        <th>Status</th>
                                        <th style="text-align: right;">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty todayAppointments}">
                                            <c:forEach var="app" items="${todayAppointments}">
                                                <tr>
                                                    <td><strong><c:out value="${app.formattedTime}" /></strong></td>
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
                                                        <a href="${pageContext.request.contextPath}/doctor/records?patientId=${app.patientId}&appointmentId=${app.id}" class="btn btn-primary btn-sm">
                                                            Record EHR
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 32px;">
                                                    No consultations scheduled for today. Take time to review upcoming appointments!
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Recent Patient Feedback Widget -->
                    <div class="bento-card col-span-4 reveal-card" style="--i: 5;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                            <div>
                                <h3 style="font-size: 1.15rem;">Recent Reviews</h3>
                                <p style="font-size: 0.82rem; color: var(--text-muted);">What patients say</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/doctor/feedback" class="btn btn-ghost btn-sm">All &rarr;</a>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 14px;">
                            <c:choose>
                                <c:when test="${not empty recentFeedback}">
                                    <c:forEach var="fb" items="${recentFeedback}" begin="0" end="3">
                                        <div style="padding-bottom: 12px; border-bottom: 1px solid var(--border);">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                                                <span style="font-weight: 600; font-size: 0.86rem;"><c:out value="${fb.patientName}" /></span>
                                                <span style="color: var(--warning); font-size: 0.82rem; font-weight: 700;">★ <c:out value="${fb.rating}" />.0</span>
                                            </div>
                                            <p style="font-size: 0.82rem; color: var(--text-muted); font-style: italic;">
                                                "<c:out value="${fb.comment}" />"
                                            </p>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align: center; color: var(--text-muted); padding: 24px;">
                                        No reviews received yet.
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
