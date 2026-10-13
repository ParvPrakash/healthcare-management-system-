<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="Patient Reviews & Ratings — CareFirst Doctor" scope="request"/>
<c:set var="activeNav" value="feedback" scope="request"/>
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
                        <h1 class="page-title">Patient Feedback & Ratings</h1>
                        <p class="page-subtitle">Patient satisfaction evaluations, comments, and star rating metrics</p>
                    </div>
                </div>

                <div class="bento-grid">
                    <!-- Rating Score Highlight Card -->
                    <div class="bento-card col-span-4 stat-card reveal-card" style="--i: 0; text-align: center; align-items: center;">
                        <div class="stat-title" style="margin-bottom: 8px;">Overall Clinic Rating</div>
                        <div style="font-size: 3.5rem; font-weight: 700; color: var(--peach-700); line-height: 1.1; margin-bottom: 4px;">
                            ${avgRating}
                        </div>
                        <div style="color: var(--warning); font-size: 1.3rem; margin-bottom: 8px;">
                            ★★★★★
                        </div>
                        <div style="font-size: 0.86rem; color: var(--text-muted);">
                            From ${totalReviews} verified consultations
                        </div>
                    </div>

                    <!-- Star Distribution Card -->
                    <div class="bento-card col-span-8 reveal-card" style="--i: 1;">
                        <h3 style="margin-bottom: 16px;">Rating Breakdown</h3>
                        <div style="display: flex; flex-direction: column; gap: 10px;">
                            <c:forEach begin="1" end="5" var="i">
                                <c:set var="starLevel" value="${6 - i}" />
                                <c:set var="count" value="${distribution[starLevel] != null ? distribution[starLevel] : 0}" />
                                <c:set var="pct" value="${totalReviews > 0 ? (count * 100 / totalReviews) : 0}" />
                                <div style="display: flex; align-items: center; gap: 12px; font-size: 0.88rem;">
                                    <span style="width: 50px; font-weight: 600;">${starLevel} ★</span>
                                    <div style="flex: 1; height: 10px; background: var(--surface-soft); border-radius: 5px; overflow: hidden; border: 1px solid var(--border);">
                                        <div style="height: 100%; width: ${pct}%; background: linear-gradient(90deg, var(--peach-400), var(--peach-600)); border-radius: 5px;"></div>
                                    </div>
                                    <span style="width: 40px; text-align: right; color: var(--text-muted); font-size: 0.82rem;">${count}</span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                    <!-- Patient Reviews Feed -->
                    <div class="bento-card col-span-12 reveal-card" style="--i: 2;">
                        <h3 style="margin-bottom: 18px;">Patient Testimonials & Reviews</h3>
                        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 18px;">
                            <c:choose>
                                <c:when test="${not empty feedbackList}">
                                    <c:forEach var="fb" items="${feedbackList}">
                                        <div style="background: var(--surface-soft); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 18px;">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                                                <span style="font-weight: 600;"><c:out value="${fb.patientName}" /></span>
                                                <span style="color: var(--warning); font-size: 0.85rem; font-weight: 700;">
                                                    <c:forEach begin="1" end="${fb.rating}">★</c:forEach>
                                                </span>
                                            </div>
                                            <p style="font-size: 0.88rem; color: var(--text); line-height: 1.5; font-style: italic;">
                                                "<c:out value="${fb.comment}" />"
                                            </p>
                                            <div style="font-size: 0.75rem; color: var(--text-faint); margin-top: 10px; text-align: right;">
                                                <c:out value="${fb.createdAt}" />
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div style="grid-column: 1 / -1; text-align: center; color: var(--text-muted); padding: 32px;">
                                        No reviews posted yet. Completed consultations will prompt patients for feedback!
                                    </div>
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
