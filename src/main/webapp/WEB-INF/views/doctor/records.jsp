<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Electronic Health Records (EMR) — CareFirst Doctor" scope="request"/>
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
                        <h1 class="page-title">Electronic Medical Records (EMR)</h1>
                        <p class="page-subtitle">Clinical charting, diagnostic assessment, vital signs recording, and digital prescriptions</p>
                    </div>
                </div>

                <div class="bento-grid">
                    <!-- EHR Form -->
                    <div class="bento-card col-span-7 reveal-card" style="--i: 0;">
                        <h3 style="margin-bottom: 18px; border-bottom: 1px solid var(--border); padding-bottom: 10px;">
                            ✍️ Clinical Encounter Entry
                        </h3>

                        <form action="${pageContext.request.contextPath}/records/save" method="post">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="appointmentId" value="${param.appointmentId}">

                            <div class="form-group">
                                <label class="form-label">Patient ID *</label>
                                <input type="number" name="patientId" class="form-control"
                                       value="${not empty selectedPatient ? selectedPatient.id : param.patientId}"
                                       placeholder="Enter Patient ID (e.g. 7)" required>
                                <c:if test="${not empty selectedPatient}">
                                    <div style="font-size: 0.82rem; color: var(--peach-700); margin-top: 4px; font-weight: 600;">
                                        Patient: <c:out value="${selectedPatient.name}" /> (${selectedPatient.age} yrs, ${selectedPatient.gender}, Blood: ${selectedPatient.bloodGroup})
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Primary Diagnosis *</label>
                                <input type="text" name="diagnosis" class="form-control" placeholder="e.g. Essential Hypertension (I10) or Acute Sinusitis" required>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px;">
                                <div class="form-group">
                                    <label class="form-label">Blood Pressure</label>
                                    <input type="text" name="vitalsBloodPressure" class="form-control" placeholder="120/80">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Heart Rate (BPM)</label>
                                    <input type="text" name="vitalsHeartRate" class="form-control" placeholder="72">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Temp (°F)</label>
                                    <input type="text" name="vitalsTemperature" class="form-control" placeholder="98.6">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Prescription & Medication Plan *</label>
                                <textarea name="prescription" class="form-control" rows="3" placeholder="e.g. 1. Amlodipine 5mg OD x 30 days&#10;2. Lifestyle: Sodium restriction <2g/day" required></textarea>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Clinical Progress Notes</label>
                                <textarea name="notes" class="form-control" rows="3" placeholder="Patient reports symptom onset 4 days ago. Vitals stable. Follow-up scheduled in 2 weeks."></textarea>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 14px; padding: 12px;">
                                Save & Sign Medical Record
                            </button>
                        </form>
                    </div>

                    <!-- Selected Patient History or Doctor Recent Records -->
                    <div class="bento-card col-span-5 reveal-card" style="--i: 1;">
                        <h3 style="margin-bottom: 16px;">
                            <c:choose>
                                <c:when test="${not empty selectedPatient}">
                                    History for <c:out value="${selectedPatient.name}" />
                                </c:when>
                                <c:otherwise>
                                    Your Recent Medical Records
                                </c:otherwise>
                            </c:choose>
                        </h3>

                        <div style="display: flex; flex-direction: column; gap: 16px; max-height: 600px; overflow-y: auto;">
                            <c:set var="displayList" value="${not empty patientRecords ? patientRecords : records}" />
                            <c:choose>
                                <c:when test="${not empty displayList}">
                                    <c:forEach var="rec" items="${displayList}">
                                        <div style="background: var(--surface-soft); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 16px;">
                                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                                                <div style="font-weight: 700; color: var(--peach-700);"><c:out value="${rec.diagnosis}" /></div>
                                                <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${rec.recordDate}" /></div>
                                            </div>
                                            <div style="font-size: 0.8rem; color: var(--text-faint); margin-bottom: 8px;">
                                                Patient: <strong><c:out value="${rec.patientName}" /></strong>
                                                <c:if test="${not empty rec.vitalsBloodPressure}">
                                                    • BP: <c:out value="${rec.vitalsBloodPressure}" />
                                                </c:if>
                                            </div>
                                            <div style="font-size: 0.84rem; margin-bottom: 6px;">
                                                <strong>Rx:</strong> <c:out value="${rec.prescription}" />
                                            </div>
                                            <c:if test="${not empty rec.notes}">
                                                <div style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">
                                                    Notes: <c:out value="${rec.notes}" />
                                                </div>
                                            </c:if>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <p style="color: var(--text-muted); font-size: 0.88rem; text-align: center; padding: 24px;">
                                        No previous records on file.
                                    </p>
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
