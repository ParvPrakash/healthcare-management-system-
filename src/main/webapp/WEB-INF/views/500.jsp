<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Internal Server Error (500) — CareFirst" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body class="auth-page">
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <div class="auth-card reveal-card" style="text-align: center; max-width: 520px;">
        <div style="font-size: 4rem; font-family: var(--font-serif); font-weight: 700; color: var(--danger); margin-bottom: 12px;">
            500
        </div>
        <h2 style="margin-bottom: 12px;">System Service Interrupted</h2>
        <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 24px; line-height: 1.6;">
            An unexpected error occurred during processing. Our automated audit logger has recorded this incident for review.
        </p>

        <c:if test="${not empty errorMessage}">
            <div style="background: var(--surface-soft); border: 1px solid var(--border); padding: 12px; border-radius: var(--radius-sm); font-size: 0.82rem; font-family: monospace; color: var(--danger); margin-bottom: 24px; text-align: left; word-break: break-all;">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <div style="display: flex; gap: 12px; justify-content: center;">
            <button type="button" onclick="history.back()" class="btn btn-secondary">Go Back</button>
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Safety</a>
        </div>
    </div>
</body>
</html>
