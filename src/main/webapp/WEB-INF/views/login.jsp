<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Sign In — CareFirst Health Center" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body class="auth-page">
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <div class="auth-card reveal-card" style="--i: 0;">
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
            <h2 style="font-size: 1.6rem; margin-bottom: 6px;">Welcome Back</h2>
            <p style="font-size: 0.88rem; color: var(--text-muted);">Sign in to access your clinic portal</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="toast toast-error" style="position: static; margin-bottom: 20px; width: 100%; animation: none;">
                <span class="toast-icon">✕</span>
                <span class="toast-msg"><c:out value="${errorMessage}" /></span>
            </div>
        </c:if>

        <c:if test="${param.loggedOut == 'true'}">
            <div class="toast toast-info" style="position: static; margin-bottom: 20px; width: 100%; animation: none;">
                <span class="toast-icon">ℹ</span>
                <span class="toast-msg">You have been logged out securely.</span>
            </div>
        </c:if>

        <c:if test="${param.welcome == 'true'}">
            <div class="toast toast-success" style="position: static; margin-bottom: 20px; width: 100%; animation: none;">
                <span class="toast-icon">✓</span>
                <span class="toast-msg">Registration completed! Please sign in.</span>
            </div>
        </c:if>

        <!-- Quick Fill Demo Accounts -->
        <div style="margin-bottom: 20px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <span style="font-size: 0.76rem; text-transform: uppercase; color: var(--peach-700); font-weight: 700; letter-spacing: 0.05em;">
                    🚀 1-Click Demo Credentials:
                </span>
                <span style="font-size: 0.72rem; color: var(--text-muted);">Click to auto-fill</span>
            </div>
            <div class="auth-demo-grid">
                <button type="button" class="auth-demo-btn" onclick="fillDemo('admin@carefirst.com', 'Admin@123')">
                    👑 <strong>Admin</strong><br>
                    <span style="font-size:0.72rem; color:var(--text-muted);">admin@carefirst.com</span><br>
                    <span style="font-size:0.68rem; color:var(--peach-600); font-weight:600;">Admin@123</span>
                </button>
                <button type="button" class="auth-demo-btn" onclick="fillDemo('doctor.sarah@carefirst.com', 'Doctor@123')">
                    🩺 <strong>Doctor</strong><br>
                    <span style="font-size:0.72rem; color:var(--text-muted);">doctor.sarah@carefirst.com</span><br>
                    <span style="font-size:0.68rem; color:var(--peach-600); font-weight:600;">Doctor@123</span>
                </button>
                <button type="button" class="auth-demo-btn" onclick="fillDemo('john.doe@example.com', 'Patient@123')">
                    👤 <strong>Patient</strong><br>
                    <span style="font-size:0.72rem; color:var(--text-muted);">john.doe@example.com</span><br>
                    <span style="font-size:0.68rem; color:var(--peach-600); font-weight:600;">Patient@123</span>
                </button>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/login" method="post" id="loginForm">
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div class="form-group">
                <label class="form-label" for="loginEmail">Email Address</label>
                <input type="email" id="loginEmail" name="email" class="form-control" placeholder="name@carefirst.com" value="${email}" required autocomplete="email">
            </div>

            <div class="form-group">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <label class="form-label" for="loginPassword">Password</label>
                </div>
                <input type="password" id="loginPassword" name="password" class="form-control" placeholder="••••••••" required autocomplete="current-password">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px; padding: 12px;">
                Sign In to Portal &rarr;
            </button>
        </form>

        <div style="text-align: center; margin-top: 24px; font-size: 0.88rem; color: var(--text-muted);">
            Don't have an account? <a href="${pageContext.request.contextPath}/register" style="color: var(--peach-700); font-weight: 600; text-decoration: none;">Register as Patient</a>
        </div>
        <div style="text-align: center; margin-top: 12px; font-size: 0.82rem;">
            <a href="${pageContext.request.contextPath}/" style="color: var(--text-muted); text-decoration: none;">&larr; Return to Home</a>
        </div>
    </div>

    <script>
        function fillDemo(email, pass) {
            document.getElementById('loginEmail').value = email;
            document.getElementById('loginPassword').value = pass;
        }
    </script>

    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
