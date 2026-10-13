<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="CareFirst Health Center — Modern Compassionate Care" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/head.jspf" %>
<body>
    <div class="bg-blob-1"></div>
    <div class="bg-blob-2"></div>

    <!-- Public Landing Navbar -->
    <header class="landing-nav">
        <a href="${pageContext.request.contextPath}/" class="brand-header" style="border:none; margin:0; padding:0;">
            <div class="brand-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 2v20M2 12h20"></path>
                </svg>
            </div>
            <div>
                <div class="brand-title">CareFirst</div>
                <div class="brand-sub">Healthcare OS</div>
            </div>
        </a>

        <div style="display: flex; align-items: center; gap: 20px;">
            <button type="button" class="icon-btn" id="themeToggleBtn" title="Toggle theme" aria-label="Toggle theme">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="5"></circle>
                    <line x1="12" y1="1" x2="12" y2="3"></line>
                    <line x1="12" y1="21" x2="12" y2="23"></line>
                    <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
                    <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
                    <line x1="1" y1="12" x2="3" y2="12"></line>
                    <line x1="21" y1="12" x2="23" y2="12"></line>
                    <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
                    <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
                </svg>
            </button>

            <c:choose>
                <c:when test="${not empty sessionScope.USER}">
                    <a href="${pageContext.request.contextPath}${sessionScope.USER.dashboardPath}" class="btn btn-primary btn-sm">
                        Go to Dashboard &rarr;
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-sm">Sign In</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Create Account</a>
                </c:otherwise>
            </c:choose>
        </div>
    </header>

    <!-- Hero Section -->
    <main>
        <section class="landing-hero reveal-card" style="--i: 0;">
            <div class="hero-pill">
                <span>✦</span>
                <span>${not empty banner ? banner : 'Compassionate Healthcare Reimagined'}</span>
            </div>
            <h1 class="hero-title">
                World-class clinical expertise,<br>
                <span>effortlessly scheduled.</span>
            </h1>
            <p class="hero-subtitle">
                ${not empty tagline ? tagline : 'Experience personalized medicine, certified specialist doctors, zero waiting lines, and instant digital medical records all in one warm, secure platform.'}
            </p>
            <div class="hero-actions">
                <a href="${pageContext.request.contextPath}/register" class="btn btn-primary" style="padding: 14px 28px; font-size: 1rem;">
                    Book Consultation Now
                </a>
                <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary" style="padding: 14px 28px; font-size: 1rem;">
                    Explore Demo Portals
                </a>
            </div>

            <!-- Bento Stats Ribbon -->
            <div class="bento-grid" style="margin-top: 60px; text-align: left;">
                <div class="bento-card col-span-3 stat-card">
                    <div class="stat-header">
                        <span class="stat-title">Board Certified</span>
                        <div class="stat-icon">👨‍⚕️</div>
                    </div>
                    <div class="stat-value"><span data-counter="18">0</span>+</div>
                    <div class="stat-delta positive">Top tier specialists</div>
                </div>

                <div class="bento-card col-span-3 stat-card">
                    <div class="stat-header">
                        <span class="stat-title">Happy Patients</span>
                        <div class="stat-icon">❤️</div>
                    </div>
                    <div class="stat-value"><span data-counter="2400">0</span>+</div>
                    <div class="stat-delta positive">98.4% satisfaction rate</div>
                </div>

                <div class="bento-card col-span-3 stat-card">
                    <div class="stat-header">
                        <span class="stat-title">Avg. Wait Time</span>
                        <div class="stat-icon">⏱️</div>
                    </div>
                    <div class="stat-value">&lt; <span data-counter="5">0</span>m</div>
                    <div class="stat-delta positive">Real-time scheduling</div>
                </div>

                <div class="bento-card col-span-3 stat-card">
                    <div class="stat-header">
                        <span class="stat-title">Digital EMR</span>
                        <div class="stat-icon">🔒</div>
                    </div>
                    <div class="stat-value">100%</div>
                    <div class="stat-delta positive">HIPAA-ready security</div>
                </div>
            </div>
        </section>

        <!-- Featured Specialists Section -->
        <section class="content-body" style="padding-top: 0;">
            <div class="page-header" style="text-align: center; justify-content: center; margin-bottom: 36px;">
                <div>
                    <h2 style="font-size: 2.2rem; margin-bottom: 8px;">Featured Specialists</h2>
                    <p class="page-subtitle" style="font-size: 1rem;">
                        Meet our acclaimed physicians across cardiology, neurology, pediatrics, and more.
                    </p>
                </div>
            </div>

            <div class="doctor-grid">
                <c:forEach var="doc" items="${featuredDoctors}" varStatus="status">
                    <div class="doctor-card reveal-card" style="--i: ${status.index};">
                        <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 16px;">
                            <div class="doctor-avatar">
                                <c:out value="${doc.name.substring(0, 1)}" />
                            </div>
                            <div>
                                <h3 style="font-size: 1.15rem; margin-bottom: 2px;"><c:out value="${doc.name}" /></h3>
                                <div class="chip status-peach"><c:out value="${doc.specialization}" /></div>
                            </div>
                        </div>
                        <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 18px; line-height: 1.5; flex: 1;">
                            <c:out value="${doc.bio}" />
                        </p>
                        <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 14px; border-top: 1px solid var(--border); font-size: 0.85rem;">
                            <div>
                                <span style="color: var(--warning); font-weight: 700;">★ <fmt:formatNumber value="${doc.rating}" minFractionDigits="1" maxFractionDigits="1" /></span>
                                <span style="color: var(--text-faint);">(${doc.reviewCount} reviews)</span>
                            </div>
                            <div style="font-weight: 700; color: var(--peach-700);">
                                $<fmt:formatNumber value="${doc.consultationFee}" minFractionDigits="0" />
                            </div>
                        </div>
                        <div style="margin-top: 16px;">
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-sm" style="width: 100%;">
                                Book Consultation
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </section>

        <!-- Services & Features Bento -->
        <section class="content-body" style="padding-top: 40px; padding-bottom: 80px;">
            <div class="page-header" style="text-align: center; justify-content: center; margin-bottom: 36px;">
                <div>
                    <h2 style="font-size: 2.2rem; margin-bottom: 8px;">Comprehensive Care, End-to-End</h2>
                    <p class="page-subtitle" style="font-size: 1rem;">
                        Designed for clinicians, administrators, and patients alike.
                    </p>
                </div>
            </div>

            <div class="bento-grid">
                <div class="bento-card col-span-4">
                    <div style="font-size: 2rem; margin-bottom: 12px;">🗓️</div>
                    <h3 style="margin-bottom: 8px;">Concurrent Slot Lock Safety</h3>
                    <p style="font-size: 0.88rem; color: var(--text-muted); line-height: 1.6;">
                        High-concurrency distributed lock ensures double-booking is impossible even during peak appointment surges.
                    </p>
                </div>

                <div class="bento-card col-span-4">
                    <div style="font-size: 2rem; margin-bottom: 12px;">📊</div>
                    <h3 style="margin-bottom: 8px;">Real-Time Clinical Analytics</h3>
                    <p style="font-size: 0.88rem; color: var(--text-muted); line-height: 1.6;">
                        Executive dashboards tracking clinic throughput, doctor ratings, revenue projections, and patient trends.
                    </p>
                </div>

                <div class="bento-card col-span-4">
                    <div style="font-size: 2rem; margin-bottom: 12px;">📋</div>
                    <h3 style="margin-bottom: 8px;">Digital EHR & Prescriptions</h3>
                    <p style="font-size: 0.88rem; color: var(--text-muted); line-height: 1.6;">
                        Structured patient medical records, vitals tracking (BP, heart rate, temp), and instant printable prescriptions.
                    </p>
                </div>
            </div>

            <!-- Call to action card -->
            <div class="bento-card col-span-12" style="background: linear-gradient(135deg, var(--surface-soft), var(--peach-100)); text-align: center; padding: 48px 24px; border-color: var(--peach-300);">
                <h2 style="font-size: 2.2rem; margin-bottom: 12px;">Ready to elevate your healthcare experience?</h2>
                <p style="color: var(--text-muted); max-width: 600px; margin: 0 auto 28px; font-size: 1.05rem;">
                    Join thousands of patients receiving compassionate, top-rated care at CareFirst.
                </p>
                <div style="display: flex; gap: 14px; justify-content: center;">
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-primary" style="padding: 12px 28px;">
                        Create Your Patient Portal
                    </a>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary" style="padding: 12px 28px;">
                        Staff & Admin Sign In
                    </a>
                </div>
            </div>
        </section>
    </main>

    <!-- Footer -->
    <footer style="border-top: 1px solid var(--border); padding: 32px 24px; text-align: center; color: var(--text-muted); font-size: 0.86rem; background: var(--surface);">
        <p>&copy; 2026 CareFirst Health Center. Secure Jakarta Servlet 6.0 & HikariCP Enterprise Architecture.</p>
    </footer>

    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
