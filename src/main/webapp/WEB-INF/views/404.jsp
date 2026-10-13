<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Page Not Found (404) — CareFirst" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body class="auth-page">
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <div class="auth-card reveal-card" style="text-align: center; max-width: 480px;">
        <div style="font-size: 4rem; font-family: var(--font-serif); font-weight: 700; color: var(--peach-600); margin-bottom: 12px;">
            404
        </div>
        <h2 style="margin-bottom: 12px;">Page Not Found</h2>
        <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 28px; line-height: 1.6;">
            The clinical record, appointment link, or page you requested cannot be found or has been relocated.
        </p>
        <div style="display: flex; gap: 12px; justify-content: center;">
            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Home Page</a>
            <c:if test="${not empty sessionScope.USER}">
                <a href="${pageContext.request.contextPath}${sessionScope.USER.dashboardPath}" class="btn btn-primary">My Dashboard</a>
            </c:if>
        </div>
    </div>
</body>
</html>
