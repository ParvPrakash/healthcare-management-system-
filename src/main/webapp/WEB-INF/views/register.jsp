<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Register Patient — CareFirst Health Center" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body class="auth-page">
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <div class="auth-card reveal-card" style="max-width: 620px; --i: 0;">
        <div class="auth-header">
            <a href="${pageContext.request.contextPath}/" class="auth-logo">
                <div class="brand-icon">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M12 2v20M2 12h20"></path>
                    </svg>
                </div>
                <div style="text-align: left;">
                    <div class="brand-title">CareFirst</div>
                    <div class="brand-sub">Healthcare OS</div>
                </div>
            </a>
            <h2 style="font-size: 1.6rem; margin-bottom: 6px;">New Patient Registration</h2>
            <p style="font-size: 0.88rem; color: var(--text-muted);">Create your patient account to book consultations and access medical records</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="toast toast-error" style="position: static; margin-bottom: 20px; width: 100%; animation: none;">
                <span class="toast-icon">✕</span>
                <span class="toast-msg"><c:out value="${errorMessage}" /></span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm">
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <div class="form-group">
                    <label class="form-label" for="regName">Full Legal Name *</label>
                    <input type="text" id="regName" name="name" class="form-control" placeholder="e.g. Eleanor Vance" value="${patient.name}" required>
                    <c:if test="${not empty fieldErrors.name}">
                        <span class="form-error-msg">${fieldErrors.name}</span>
                    </c:if>
                </div>

                <div class="form-group">
                    <label class="form-label" for="regEmail">Email Address *</label>
                    <input type="email" id="regEmail" name="email" class="form-control" placeholder="eleanor@example.com" value="${patient.email}" required>
                    <c:if test="${not empty fieldErrors.email}">
                        <span class="form-error-msg">${fieldErrors.email}</span>
                    </c:if>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <div class="form-group">
                    <label class="form-label" for="regPassword">Password *</label>
                    <input type="password" id="regPassword" name="password" class="form-control" placeholder="Min 6 characters" required>
                    <c:if test="${not empty fieldErrors.password}">
                        <span class="form-error-msg">${fieldErrors.password}</span>
                    </c:if>
                </div>

                <div class="form-group">
                    <label class="form-label" for="regPhone">Phone Number *</label>
                    <input type="tel" id="regPhone" name="phone" class="form-control" placeholder="+1 (555) 000-0000" value="${patient.phone}" required>
                    <c:if test="${not empty fieldErrors.phone}">
                        <span class="form-error-msg">${fieldErrors.phone}</span>
                    </c:if>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
                <div class="form-group">
                    <label class="form-label" for="regAge">Age</label>
                    <input type="number" id="regAge" name="age" class="form-control" min="0" max="130" placeholder="e.g. 32" value="${patient.age}">
                </div>

                <div class="form-group">
                    <label class="form-label" for="regGender">Gender</label>
                    <select id="regGender" name="gender" class="form-control">
                        <option value="FEMALE" ${patient.gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                        <option value="MALE" ${patient.gender == 'MALE' ? 'selected' : ''}>Male</option>
                        <option value="OTHER" ${patient.gender == 'OTHER' ? 'selected' : ''}>Other</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="regBlood">Blood Group</label>
                    <select id="regBlood" name="bloodGroup" class="form-control">
                        <option value="">Unknown</option>
                        <option value="A+" ${patient.bloodGroup == 'A+' ? 'selected' : ''}>A+</option>
                        <option value="A-" ${patient.bloodGroup == 'A-' ? 'selected' : ''}>A-</option>
                        <option value="B+" ${patient.bloodGroup == 'B+' ? 'selected' : ''}>B+</option>
                        <option value="B-" ${patient.bloodGroup == 'B-' ? 'selected' : ''}>B-</option>
                        <option value="AB+" ${patient.bloodGroup == 'AB+' ? 'selected' : ''}>AB+</option>
                        <option value="AB-" ${patient.bloodGroup == 'AB-' ? 'selected' : ''}>AB-</option>
                        <option value="O+" ${patient.bloodGroup == 'O+' ? 'selected' : ''}>O+</option>
                        <option value="O-" ${patient.bloodGroup == 'O-' ? 'selected' : ''}>O-</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="regAddress">Residential Address</label>
                <input type="text" id="regAddress" name="address" class="form-control" placeholder="123 Peach Blossom Way, Suite 4" value="${patient.address}">
            </div>

            <div class="form-group">
                <label class="form-label" for="regEmergency">Emergency Contact (Name & Phone)</label>
                <input type="text" id="regEmergency" name="emergencyContact" class="form-control" placeholder="e.g. Arthur Vance (+1 555-111-2222)" value="${patient.emergencyContact}">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px; padding: 12px;">
                Complete Registration & Sign In &rarr;
            </button>
        </form>

        <div style="text-align: center; margin-top: 24px; font-size: 0.88rem; color: var(--text-muted);">
            Already have an account? <a href="${pageContext.request.contextPath}/login" style="color: var(--peach-700); font-weight: 600; text-decoration: none;">Sign In</a>
        </div>
        <div style="text-align: center; margin-top: 12px; font-size: 0.82rem;">
            <a href="${pageContext.request.contextPath}/" style="color: var(--text-muted); text-decoration: none;">&larr; Return to Home</a>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
