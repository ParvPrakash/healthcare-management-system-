<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="System & Clinic Settings — CareFirst Admin" scope="request"/>
<c:set var="activeNav" value="settings" scope="request"/>
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
                        <h1 class="page-title">Clinic Configuration & System Policies</h1>
                        <p class="page-subtitle">Configure operating hours, booking rules, clinic branding, and notification triggers</p>
                    </div>
                </div>

                <div class="bento-grid">
                    <div class="bento-card col-span-8 reveal-card" style="--i: 0;">
                        <form action="${pageContext.request.contextPath}/admin/settings" method="post">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <h3 style="margin-bottom: 18px; border-bottom: 1px solid var(--border); padding-bottom: 10px;">
                                🏥 Clinic Identity & Contact
                            </h3>

                            <div class="form-group">
                                <label class="form-label">Clinic Facility Name</label>
                                <input type="text" name="clinic_name" class="form-control" value="<c:out value='${settings.clinic_name}' />" required>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                                <div class="form-group">
                                    <label class="form-label">Support Email</label>
                                    <input type="email" name="clinic_email" class="form-control" value="<c:out value='${settings.clinic_email}' />">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Reception Phone</label>
                                    <input type="tel" name="clinic_phone" class="form-control" value="<c:out value='${settings.clinic_phone}' />">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Physical Clinic Address</label>
                                <input type="text" name="clinic_address" class="form-control" value="<c:out value='${settings.clinic_address}' />">
                            </div>

                            <h3 style="margin-top: 32px; margin-bottom: 18px; border-bottom: 1px solid var(--border); padding-bottom: 10px;">
                                ⏱️ Scheduling & Operating Policy
                            </h3>

                            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
                                <div class="form-group">
                                    <label class="form-label">Slot Duration (Min)</label>
                                    <input type="number" name="slot_duration_minutes" class="form-control" value="<c:out value='${settings.slot_duration_minutes}' />">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Working Hours Start</label>
                                    <input type="time" name="working_hours_start" class="form-control" value="<c:out value='${settings.working_hours_start}' />">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Working Hours End</label>
                                    <input type="time" name="working_hours_end" class="form-control" value="<c:out value='${settings.working_hours_end}' />">
                                </div>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                                <div class="form-group">
                                    <label class="form-label">Max Bookings / Patient / Day</label>
                                    <input type="number" name="max_bookings_per_day" class="form-control" value="<c:out value='${settings.max_bookings_per_day}' />">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Session Timeout (Minutes)</label>
                                    <input type="number" name="session_timeout_minutes" class="form-control" value="<c:out value='${settings.session_timeout_minutes}' />">
                                </div>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 12px;">
                                <div class="form-group">
                                    <label class="form-label">Automated Reminders</label>
                                    <select name="notifications_enabled" class="form-control">
                                        <option value="true" ${settings.notifications_enabled == 'true' ? 'selected' : ''}>Enabled (Active Background Poller)</option>
                                        <option value="false" ${settings.notifications_enabled == 'false' ? 'selected' : ''}>Disabled</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Maintenance Mode</label>
                                    <select name="maintenance_mode" class="form-control">
                                        <option value="false" ${settings.maintenance_mode == 'false' ? 'selected' : ''}>Normal Live Operation</option>
                                        <option value="true" ${settings.maintenance_mode == 'true' ? 'selected' : ''}>Active (Maintenance Window)</option>
                                    </select>
                                </div>
                            </div>

                            <div style="margin-top: 28px;">
                                <button type="submit" class="btn btn-primary" style="padding: 12px 28px;">
                                    Save System Configuration
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- System Info Sidebar Box -->
                    <div class="bento-card col-span-4 reveal-card" style="--i: 1;">
                        <h3 style="margin-bottom: 16px;">Runtime Health</h3>
                        <div style="display: flex; flex-direction: column; gap: 16px; font-size: 0.88rem;">
                            <div style="display: flex; justify-content: space-between; padding-bottom: 10px; border-bottom: 1px solid var(--border);">
                                <span style="color: var(--text-muted);">Database Pool:</span>
                                <span class="chip status-success">HikariCP 20 Max</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; padding-bottom: 10px; border-bottom: 1px solid var(--border);">
                                <span style="color: var(--text-muted);">Web Container:</span>
                                <span style="font-weight: 600;">Tomcat 10.1 (Jakarta 6.0)</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; padding-bottom: 10px; border-bottom: 1px solid var(--border);">
                                <span style="color: var(--text-muted);">Concurreny Engine:</span>
                                <span style="font-weight: 600;">ReentrantReadWriteLock</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; padding-bottom: 10px; border-bottom: 1px solid var(--border);">
                                <span style="color: var(--text-muted);">Audit Trail:</span>
                                <span class="chip status-peach">Active</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: var(--text-muted);">Session Defense:</span>
                                <span class="chip status-info">Fixation Protected</span>
                            </div>
                    </div>

                    <!-- Friend Demo Management Box -->
                    <div class="bento-card col-span-4 reveal-card" style="--i: 2; border-color: var(--peach-300); background: linear-gradient(135deg, var(--surface), var(--peach-50));">
                        <h3 style="margin-bottom: 12px; color: var(--peach-700); font-size: 1.15rem;">
                            🔄 Demo Data Restore
                        </h3>
                        <p style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5; margin-bottom: 16px;">
                            Restore all doctors, sample appointments, medical records, and reset the IP registration limiter back to the initial demo baseline.
                        </p>

                        <c:if test="${param.demoReset == 'true'}">
                            <div class="toast toast-success" style="position: static; margin-bottom: 16px; width: 100%; animation: none;">
                                <span class="toast-icon">✓</span>
                                <span class="toast-msg">Database restored to pristine demo state!</span>
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/admin/reset-demo" method="post" onsubmit="return confirm('⚠️ Are you sure you want to restore the demo database? All user test bookings and newly created accounts will be reset to default.');">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn btn-secondary" style="width: 100%; justify-content: center; border-color: var(--peach-400); color: var(--peach-700);">
                                ⚡ Reset Demo Database
                            </button>
                        </form>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
