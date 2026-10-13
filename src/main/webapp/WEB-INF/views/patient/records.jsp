<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="My Medical Records & Prescriptions — CareFirst Patient" scope="request"/>
<c:set var="activeNav" value="records" scope="request"/>
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
                        <h1 class="page-title">Personal Health Records & Prescriptions</h1>
                        <p class="page-subtitle">Your comprehensive Electronic Health Records (EHR), clinical findings, and physician prescriptions</p>
                    </div>
                    <div class="header-actions">
                        <button type="button" class="btn btn-secondary btn-sm" onclick="window.print()">
                            🖨️ Print Clinical Summary
                        </button>
                    </div>
                </div>

                <div class="bento-grid">
                    <c:choose>
                        <c:when test="${not empty records}">
                            <c:forEach var="rec" items="${records}" varStatus="status">
                                <div class="bento-card col-span-12 reveal-card" style="--i: ${status.index};">
                                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; border-bottom: 1px solid var(--border); padding-bottom: 12px; flex-wrap: wrap; gap: 10px;">
                                        <div>
                                            <div class="chip status-peach" style="margin-bottom: 6px;">Consultation Record</div>
                                            <h3 style="font-size: 1.35rem; margin-bottom: 4px;"><c:out value="${rec.diagnosis}" /></h3>
                                            <p style="font-size: 0.85rem; color: var(--text-muted);">
                                                Attending Physician: <strong>Dr. <c:out value="${rec.doctorName}" /></strong>
                                            </p>
                                        </div>
                                        <div style="text-align: right;">
                                            <div style="font-weight: 600;"><c:out value="${rec.recordDate}" /></div>
                                            <span class="chip status-success" style="margin-top: 4px;">Verified Record</span>
                                        </div>
                                    </div>

                                    <!-- Vitals row -->
                                    <div style="display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 18px; background: var(--surface-soft); padding: 12px 18px; border-radius: var(--radius-md);">
                                        <c:if test="${not empty rec.vitalsBloodPressure}">
                                            <div>
                                                <span style="font-size: 0.76rem; color: var(--text-muted); text-transform: uppercase;">Blood Pressure</span>
                                                <div style="font-weight: 700; font-size: 1.05rem;"><c:out value="${rec.vitalsBloodPressure}" /> mmHg</div>
                                            </div>
                                        </c:if>
                                        <c:if test="${not empty rec.vitalsHeartRate}">
                                            <div>
                                                <span style="font-size: 0.76rem; color: var(--text-muted); text-transform: uppercase;">Heart Rate</span>
                                                <div style="font-weight: 700; font-size: 1.05rem;"><c:out value="${rec.vitalsHeartRate}" /> BPM</div>
                                            </div>
                                        </c:if>
                                        <c:if test="${not empty rec.vitalsTemperature}">
                                            <div>
                                                <span style="font-size: 0.76rem; color: var(--text-muted); text-transform: uppercase;">Body Temperature</span>
                                                <div style="font-weight: 700; font-size: 1.05rem;"><c:out value="${rec.vitalsTemperature}" /> °F</div>
                                            </div>
                                        </c:if>
                                    </div>

                                    <!-- Prescription & Notes -->
                                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                                        <div>
                                            <h4 style="font-size: 0.95rem; margin-bottom: 8px; color: var(--peach-700);">💊 Prescription & Dosage</h4>
                                            <div style="background: var(--surface); border: 1px solid var(--border); padding: 14px; border-radius: var(--radius-md); font-size: 0.88rem; line-height: 1.6; white-space: pre-line;">
                                                <c:out value="${rec.prescription}" />
                                            </div>
                                        </div>

                                        <div>
                                            <h4 style="font-size: 0.95rem; margin-bottom: 8px; color: var(--text-muted);">📝 Clinical Notes</h4>
                                            <div style="background: var(--surface); border: 1px solid var(--border); padding: 14px; border-radius: var(--radius-md); font-size: 0.88rem; line-height: 1.6; color: var(--text-muted); font-style: italic;">
                                                <c:out value="${not empty rec.notes ? rec.notes : 'No extra notes recorded.'}" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="bento-card col-span-12 reveal-card" style="text-align: center; padding: 48px; --i: 0;">
                                <div style="font-size: 3rem; margin-bottom: 12px;">📁</div>
                                <h3 style="margin-bottom: 8px;">No Medical Records Found</h3>
                                <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 20px;">
                                    Records, test results, and prescriptions written by your doctors during consultations will appear here automatically.
                                </p>
                                <a href="${pageContext.request.contextPath}/patient/doctors" class="btn btn-primary btn-sm">Book an Appointment &rarr;</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
