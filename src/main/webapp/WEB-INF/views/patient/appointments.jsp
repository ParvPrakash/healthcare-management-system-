<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="My Appointments — CareFirst Patient" scope="request"/>
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
                        <h1 class="page-title">My Appointments & Consultations</h1>
                        <p class="page-subtitle">Track your upcoming visits, manage bookings, and leave doctor feedback</p>
                    </div>
                    <div class="header-actions">
                        <a href="${pageContext.request.contextPath}/patient/doctors" class="btn btn-primary btn-sm">
                            + Book New Appointment
                        </a>
                    </div>
                </div>

                <!-- Filter Toolbar -->
                <div class="filter-bar">
                    <form action="${pageContext.request.contextPath}/patient/appointments" method="get" class="filter-group">
                        <select name="status" class="form-control" onchange="this.form.submit()" style="width: auto;">
                            <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Appointments</option>
                            <option value="CONFIRMED" ${selectedStatus == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                            <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                        </select>
                    </form>
                </div>

                <!-- Appointments Table -->
                <div class="table-container">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Date & Time</th>
                                <th>Doctor</th>
                                <th>Specialty</th>
                                <th>Reason for Visit</th>
                                <th>Status</th>
                                <th>Fee</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty appointments}">
                                    <c:forEach var="app" items="${appointments}">
                                        <tr>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${app.appointmentDate}" /></div>
                                                <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${app.formattedTime}" /></div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;">Dr. <c:out value="${app.doctorName}" /></div>
                                            </td>
                                            <td><c:out value="${app.specialization}" /></td>
                                            <td><c:out value="${app.reasonForVisit}" /></td>
                                            <td>
                                                <span class="chip ${app.statusBadgeClass}">
                                                    <c:out value="${app.status.displayName}" />
                                                </span>
                                            </td>
                                            <td>$<fmt:formatNumber value="${app.consultationFee}" minFractionDigits="0"/></td>
                                            <td style="text-align: right;">
                                                <div style="display: inline-flex; gap: 8px;">
                                                    <!-- If completed, allow leaving feedback -->
                                                    <c:if test="${app.status == 'COMPLETED'}">
                                                        <button type="button" class="btn btn-secondary btn-sm"
                                                                onclick="openFeedbackModal('${app.id}', '${app.doctorId}', '${app.doctorName}')">
                                                            ★ Review Doctor
                                                        </button>
                                                    </c:if>

                                                    <!-- If confirmed or pending, allow cancellation -->
                                                    <c:if test="${app.status == 'CONFIRMED' or app.status == 'PENDING'}">
                                                        <form action="${pageContext.request.contextPath}/appointments/status" method="post" style="display:inline;" onsubmit="return confirm('Cancel this appointment?');">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="appointmentId" value="${app.id}">
                                                            <input type="hidden" name="status" value="CANCELLED">
                                                            <button type="submit" class="btn btn-ghost btn-sm" style="color: var(--danger);">
                                                                Cancel
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 36px;">
                                            No appointments found. <a href="${pageContext.request.contextPath}/patient/doctors" style="color: var(--peach-700); font-weight: 600;">Book a doctor now &rarr;</a>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    </div>

    <!-- Modal: Leave Doctor Review -->
    <div id="feedbackModal" class="modal-backdrop" role="dialog" aria-modal="true">
        <div class="modal-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h3 style="font-size: 1.3rem;">Doctor Consultation Review</h3>
                <button type="button" class="btn btn-ghost btn-sm modal-close">&times;</button>
            </div>

            <form action="${pageContext.request.contextPath}/patient/feedback" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="appointmentId" id="modalAppointmentId">
                <input type="hidden" name="doctorId" id="modalDoctorId">

                <div style="margin-bottom: 16px;">
                    Reviewing: <strong id="modalDoctorName" style="color: var(--peach-700);">Dr. Clinician</strong>
                </div>

                <div class="form-group">
                    <label class="form-label">Rating (1 to 5 Stars) *</label>
                    <select name="rating" class="form-control" required>
                        <option value="5" selected>★★★★★ - Exceptional (5/5)</option>
                        <option value="4">★★★★☆ - Very Good (4/5)</option>
                        <option value="3">★★★☆☆ - Average (3/5)</option>
                        <option value="2">★★☆☆☆ - Below Average (2/5)</option>
                        <option value="1">★☆☆☆☆ - Unsatisfactory (1/5)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label">Review Comment *</label>
                    <textarea name="comment" class="form-control" rows="3" placeholder="Share your experience regarding punctuality, bedside manner, and diagnosis clarity..." required></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
                    <button type="button" class="btn btn-secondary modal-close">Cancel</button>
                    <button type="submit" class="btn btn-primary">Submit Review</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openFeedbackModal(appId, docId, docName) {
            document.getElementById('modalAppointmentId').value = appId;
            document.getElementById('modalDoctorId').value = docId;
            document.getElementById('modalDoctorName').textContent = docName;
            document.getElementById('feedbackModal').classList.add('open');
        }
    </script>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
