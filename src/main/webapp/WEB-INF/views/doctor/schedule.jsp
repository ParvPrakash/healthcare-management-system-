<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Doctor Schedule Manager — CareFirst" scope="request"/>
<c:set var="activeNav" value="schedule" scope="request"/>
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
                        <h1 class="page-title">Weekly Availability & Slot Generator</h1>
                        <p class="page-subtitle">Configure your recurring consulting hours and generate dynamic patient appointment slots</p>
                    </div>
                    <div class="header-actions">
                        <form action="${pageContext.request.contextPath}/doctor/schedule/generate" method="post" style="display:inline;">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn btn-secondary btn-sm">
                                ⚡ Regenerate Next 14 Days Slots
                            </button>
                        </form>
                    </div>
                </div>

                <div class="bento-grid">
                    <!-- Weekly Availability Templates Table -->
                    <div class="bento-card col-span-7 reveal-card" style="--i: 0;">
                        <h3 style="margin-bottom: 16px;">Recurring Weekly Schedule</h3>
                        <div class="table-container">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>Day of Week</th>
                                        <th>Start Time</th>
                                        <th>End Time</th>
                                        <th>Slot Interval</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty schedules}">
                                            <c:forEach var="sch" items="${schedules}">
                                                <tr>
                                                    <td><strong><c:out value="${sch.dayOfWeek}" /></strong></td>
                                                    <td><c:out value="${sch.startTime}" /></td>
                                                    <td><c:out value="${sch.endTime}" /></td>
                                                    <td><c:out value="${sch.slotDurationMinutes}" /> mins</td>
                                                    <td>
                                                        <span class="chip ${sch.active ? 'status-success' : 'status-danger'}">
                                                            <c:out value="${sch.active ? 'Active' : 'Inactive'}" />
                                                        </span>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    No recurring schedule configured yet. Add your working days on the right.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>

                        <!-- Today's Slot Breakdown -->
                        <div style="margin-top: 32px;">
                            <h3 style="margin-bottom: 12px;">Today's Generated Slots (${todayDate})</h3>
                            <c:choose>
                                <c:when test="${not empty todaySlots}">
                                    <div class="slot-grid">
                                        <c:forEach var="slot" items="${todaySlots}">
                                            <div class="slot-pill ${slot.booked ? 'booked' : ''}">
                                                <c:out value="${slot.startTime.toString().substring(0, 5)}" /> - <c:out value="${slot.endTime.toString().substring(0, 5)}" />
                                                <div style="font-size: 0.68rem; font-weight: normal; margin-top: 2px;">
                                                    <c:out value="${slot.booked ? 'Booked' : 'Available'}" />
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <p style="color: var(--text-muted); font-size: 0.88rem;">No slots generated for today. Click "Regenerate Next 14 Days Slots" above.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Add Working Day Form -->
                    <div class="bento-card col-span-5 reveal-card" style="--i: 1;">
                        <h3 style="margin-bottom: 16px;">Add / Update Working Day</h3>
                        <form action="${pageContext.request.contextPath}/doctor/schedule/save" method="post">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <div class="form-group">
                                <label class="form-label">Day of Week</label>
                                <select name="dayOfWeek" class="form-control" required>
                                    <option value="MONDAY">Monday</option>
                                    <option value="TUESDAY">Tuesday</option>
                                    <option value="WEDNESDAY">Wednesday</option>
                                    <option value="THURSDAY">Thursday</option>
                                    <option value="FRIDAY">Friday</option>
                                    <option value="SATURDAY">Saturday</option>
                                    <option value="SUNDAY">Sunday</option>
                                </select>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                                <div class="form-group">
                                    <label class="form-label">Shift Start Time</label>
                                    <input type="time" name="startTime" class="form-control" value="09:00" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Shift End Time</label>
                                    <input type="time" name="endTime" class="form-control" value="17:00" required>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Consultation Slot Duration (Minutes)</label>
                                <select name="slotDurationMinutes" class="form-control">
                                    <option value="15">15 Minutes</option>
                                    <option value="30" selected>30 Minutes (Recommended)</option>
                                    <option value="45">45 Minutes</option>
                                    <option value="60">60 Minutes</option>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 12px; padding: 12px;">
                                Save & Generate 14-Day Slots
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
