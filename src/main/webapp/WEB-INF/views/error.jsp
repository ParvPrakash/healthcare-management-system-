<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Application Notice — CareFirst" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body class="auth-page">
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <div class="auth-card reveal-card" style="text-align: center; max-width: 500px;">
        <div style="font-size: 3rem; margin-bottom: 12px;">⚠️</div>
        <h2 style="margin-bottom: 12px;">Notice</h2>
        <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 24px; line-height: 1.6;">
            <c:out value="${not empty errorMessage ? errorMessage : 'The requested action could not be completed.'}" />
        </p>
        <div style="display: flex; gap: 12px; justify-content: center;">
            <button type="button" onclick="history.back()" class="btn btn-secondary">Go Back</button>
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Dashboard / Home</a>
        </div>
    </div>
</body>
</html>
