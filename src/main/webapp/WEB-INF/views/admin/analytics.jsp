<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Performance Analytics & KPIs — CareFirst Admin" scope="request"/>
<c:set var="activeNav" value="analytics" scope="request"/>
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
                        <h1 class="page-title">Clinical Performance Analytics</h1>
                        <p class="page-subtitle">Deep metrics on patient acquisition, specialty distribution, and provider utilization</p>
                    </div>
                    <div class="header-actions">
                        <div class="filter-group">
                            <a href="${pageContext.request.contextPath}/admin/analytics?days=7" class="btn btn-sm ${selectedDays == 7 ? 'btn-primary' : 'btn-secondary'}">7 Days</a>
                            <a href="${pageContext.request.contextPath}/admin/analytics?days=14" class="btn btn-sm ${selectedDays == 14 ? 'btn-primary' : 'btn-secondary'}">14 Days</a>
                            <a href="${pageContext.request.contextPath}/admin/analytics?days=30" class="btn btn-sm ${selectedDays == 30 ? 'btn-primary' : 'btn-secondary'}">30 Days</a>
                        </div>
                    </div>
                </div>

                <!-- Bento KPI Highlights -->
                <div class="bento-grid">
                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 0;">
                        <div class="stat-header">
                            <span class="stat-title">Avg Consultation Fee</span>
                            <div class="stat-icon">💰</div>
                        </div>
                        <div class="stat-value">$<span data-counter="${avgDoctorFee}">0</span></div>
                        <div class="stat-delta positive">Across all active doctors</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 1;">
                        <div class="stat-header">
                            <span class="stat-title">Clinic Patient Base</span>
                            <div class="stat-icon">👥</div>
                        </div>
                        <div class="stat-value"><span data-counter="${overviewStats.totalPatients}">0</span></div>
                        <div class="stat-delta positive">Enrolled patients</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 2;">
                        <div class="stat-header">
                            <span class="stat-title">Total Consultations</span>
                            <div class="stat-icon">📈</div>
                        </div>
                        <div class="stat-value"><span data-counter="${overviewStats.totalAppointments}">0</span></div>
                        <div class="stat-delta positive">Lifetime visits</div>
                    </div>

                    <div class="bento-card col-span-3 stat-card reveal-card" style="--i: 3;">
                        <div class="stat-header">
                            <span class="stat-title">Clinic Revenue</span>
                            <div class="stat-icon">🏦</div>
                        </div>
                        <div class="stat-value">$<span data-counter="${overviewStats.totalRevenue}">0</span></div>
                        <div class="stat-delta positive">Settled billing</div>
                    </div>

                    <!-- Chart 1: Appointments Over Selected Window -->
                    <div class="bento-card col-span-8 reveal-card" style="--i: 4;">
                        <div style="margin-bottom: 16px;">
                            <h3 style="font-size: 1.15rem;">Appointment Volume (${selectedDays} Days)</h3>
                            <p style="font-size: 0.82rem; color: var(--text-muted);">Historical trajectory and seasonal demand</p>
                        </div>
                        <div style="height: 280px; position: relative;">
                            <canvas id="appointmentsPerDayChart"></canvas>
                        </div>
                    </div>

                    <!-- Chart 2: Status Breakdown -->
                    <div class="bento-card col-span-4 reveal-card" style="--i: 5;">
                        <div style="margin-bottom: 16px;">
                            <h3 style="font-size: 1.15rem;">Appointment Status Distribution</h3>
                            <p style="font-size: 0.82rem; color: var(--text-muted);">Ratio of completed, cancelled, and pending</p>
                        </div>
                        <div style="height: 280px; position: relative; display: flex; align-items: center; justify-content: center;">
                            <canvas id="statusDoughnutChart"></canvas>
                        </div>
                    </div>

                    <!-- Chart 3: Top Performing Physicians -->
                    <div class="bento-card col-span-12 reveal-card" style="--i: 6;">
                        <div style="margin-bottom: 16px;">
                            <h3 style="font-size: 1.15rem;">Top Clinicians by Patient Volume</h3>
                            <p style="font-size: 0.82rem; color: var(--text-muted);">Completed consultations by doctor</p>
                        </div>
                        <div style="height: 260px; position: relative;">
                            <canvas id="topDoctorsChart"></canvas>
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
