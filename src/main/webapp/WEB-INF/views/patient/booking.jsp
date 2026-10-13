<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Book Consultation — CareFirst Patient" scope="request"/>
<c:set var="activeNav" value="booking" scope="request"/>
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
                        <h1 class="page-title">Find a Specialist & Book Consultation</h1>
                        <p class="page-subtitle">Select an acclaimed physician, choose your preferred date, and lock your clinical time slot</p>
                    </div>
                </div>

                <!-- Doctor Search & Specialty Filter Toolbar -->
                <div class="filter-bar">
                    <form action="${pageContext.request.contextPath}/patient/doctors" method="get" class="filter-group" style="width: 100%; justify-content: space-between;">
                        <div class="search-input-wrap">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                            <input type="text" name="q" class="form-control" placeholder="Search doctor by name..." value="<c:out value='${keyword}' />">
                        </div>

                        <div class="filter-group">
                            <select name="spec" class="form-control" onchange="this.form.submit()" style="width: auto;">
                                <option value="ALL" ${selectedSpec == 'ALL' ? 'selected' : ''}>All Specializations</option>
                                <c:forEach var="s" items="${specializations}">
                                    <option value="${s}" ${selectedSpec == s ? 'selected' : ''}>${s}</option>
                                </c:forEach>
                            </select>

                            <button type="submit" class="btn btn-secondary btn-sm">Filter</button>
                            <c:if test="${not empty keyword or selectedSpec != 'ALL'}">
                                <a href="${pageContext.request.contextPath}/patient/doctors" class="btn btn-ghost btn-sm">Clear</a>
                            </c:if>
                        </div>
                    </form>
                </div>

                <div class="bento-grid">
                    <!-- Step 1 & 2: Doctors Grid -->
                    <div class="bento-card col-span-7 reveal-card" style="--i: 0;">
                        <h3 style="margin-bottom: 16px;">Available Specialists (${doctors.size()})</h3>
                        <div style="display: grid; grid-template-columns: 1fr; gap: 16px; max-height: 700px; overflow-y: auto; padding-right: 4px;">
                            <c:choose>
                                <c:when test="${not empty doctors}">
                                    <c:forEach var="doc" items="${doctors}">
                                        <div class="doctor-card" style="margin-bottom: 0;">
                                            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                                <div style="display: flex; gap: 14px; align-items: center;">
                                                    <div class="doctor-avatar" style="width: 48px; height: 48px; font-size: 1.1rem; margin-bottom: 0;">
                                                        <c:out value="${doc.name.substring(0, 1)}" />
                                                    </div>
                                                    <div>
                                                        <h4 style="font-size: 1.05rem;"><c:out value="${doc.name}" /></h4>
                                                        <div class="chip status-peach" style="margin-top: 2px;"><c:out value="${doc.specialization}" /></div>
                                                    </div>
                                                </div>
                                                <div style="text-align: right;">
                                                    <div style="font-weight: 700; color: var(--peach-700); font-size: 1.1rem;">
                                                        $<fmt:formatNumber value="${doc.consultationFee}" minFractionDigits="0" />
                                                    </div>
                                                    <div style="font-size: 0.78rem; color: var(--warning); font-weight: 600;">
                                                        ★ <fmt:formatNumber value="${doc.rating}" minFractionDigits="1" maxFractionDigits="1" />
                                                    </div>
                                                </div>
                                            </div>

                                            <p style="font-size: 0.84rem; color: var(--text-muted); margin: 12px 0; line-height: 1.4;">
                                                <c:out value="${doc.bio}" />
                                            </p>

                                            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 10px; font-size: 0.8rem; color: var(--text-muted);">
                                                <span>Experience: <strong>${doc.experience} yrs</strong></span>
                                                <button type="button" class="btn btn-secondary btn-sm" onclick="selectDoctor('${doc.id}', '${doc.name}')">
                                                    Select Doctor &rarr;
                                                </button>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align: center; color: var(--text-muted); padding: 48px;">
                                        No doctors found matching this specialty.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Step 3: Interactive Dynamic Slot Booking Card -->
                    <div class="bento-card col-span-5 reveal-card" style="--i: 1;">
                        <h3 style="margin-bottom: 18px; border-bottom: 1px solid var(--border); padding-bottom: 10px;">
                            📅 Reserve Time Slot
                        </h3>

                        <form action="${pageContext.request.contextPath}/appointments/book" method="post" id="bookingForm">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="slotId" id="selectedSlotId" required>

                            <div class="form-group">
                                <label class="form-label" for="bookingDoctorSelect">Select Doctor *</label>
                                <select name="doctorId" id="bookingDoctorSelect" class="form-control" required>
                                    <option value="">-- Choose a Clinician --</option>
                                    <c:forEach var="doc" items="${doctors}">
                                        <option value="${doc.id}">Dr. ${doc.name} (${doc.specialization} - $${doc.consultationFee})</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="bookingDateInput">Preferred Date *</label>
                                <input type="date" name="appointmentDate" id="bookingDateInput" class="form-control" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Available Time Slots *</label>
                                <div id="slotPickerContainer" style="min-height: 100px; background: var(--surface-soft); border: 1px dashed var(--border); border-radius: var(--radius-md); padding: 12px;">
                                    <div style="color: var(--text-muted); font-size: 0.86rem; text-align: center; padding: 24px;">
                                        Select a doctor and date to view live available slots.
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="bookingReason">Reason for Consultation *</label>
                                <textarea name="reasonForVisit" id="bookingReason" class="form-control" rows="3" placeholder="Briefly describe your symptoms or reason for visit..." required></textarea>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 14px; padding: 12px;">
                                Confirm & Book Appointment
                            </button>
                        </form>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <script>
        // Set minimum date to today
        const dateInput = document.getElementById('bookingDateInput');
        if (dateInput) {
            const today = new Date().toISOString().split('T')[0];
            dateInput.min = today;
            dateInput.value = today;
        }

        function selectDoctor(id, name) {
            const select = document.getElementById('bookingDoctorSelect');
            if (select) {
                select.value = id;
                select.dispatchEvent(new Event('change'));
                document.getElementById('bookingForm').scrollIntoView({ behavior: 'smooth' });
            }
        }
    </script>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
