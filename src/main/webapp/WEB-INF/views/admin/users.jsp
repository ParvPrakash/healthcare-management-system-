<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="User & Doctor Management — CareFirst Admin" scope="request"/>
<c:set var="activeNav" value="users" scope="request"/>
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
                        <h1 class="page-title">Users & Clinicians</h1>
                        <p class="page-subtitle">Manage accounts, clinician profiles, role authorizations, and staff onboarding</p>
                    </div>
                    <div class="header-actions">
                        <button type="button" class="btn btn-primary btn-sm" data-modal-target="addDoctorModal">
                            + Add Doctor
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/users/export" class="btn btn-secondary btn-sm">
                            📥 Export CSV
                        </a>
                    </div>
                </div>

                <!-- Filters & Search Toolbar -->
                <div class="filter-bar">
                    <form action="${pageContext.request.contextPath}/admin/users" method="get" class="filter-group" style="width: 100%; justify-content: space-between;">
                        <div class="search-input-wrap">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                            <input type="text" name="q" class="form-control" placeholder="Search by name or email..." value="<c:out value='${query}' />">
                        </div>

                        <div class="filter-group">
                            <select name="role" class="form-control" onchange="this.form.submit()" style="width: auto;">
                                <option value="ALL" ${roleFilter == 'ALL' ? 'selected' : ''}>All Roles</option>
                                <option value="ADMIN" ${roleFilter == 'ADMIN' ? 'selected' : ''}>Admins</option>
                                <option value="DOCTOR" ${roleFilter == 'DOCTOR' ? 'selected' : ''}>Doctors</option>
                                <option value="PATIENT" ${roleFilter == 'PATIENT' ? 'selected' : ''}>Patients</option>
                            </select>

                            <button type="submit" class="btn btn-secondary btn-sm">Filter</button>
                            <c:if test="${not empty query or roleFilter != 'ALL'}">
                                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-ghost btn-sm">Clear</a>
                            </c:if>
                        </div>
                    </form>
                </div>

                <!-- Users Table -->
                <div class="table-container">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>User</th>
                                <th>Role</th>
                                <th>Contact / Details</th>
                                <th>Status</th>
                                <th>Registered</th>
                                <th style="text-align: right;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty users}">
                                    <c:forEach var="u" items="${users}">
                                        <tr>
                                            <td>
                                                <div style="display: flex; align-items: center; gap: 12px;">
                                                    <div class="avatar-initials" style="background: var(--peach-100);">
                                                        <c:out value="${u.name.substring(0, 1)}" />
                                                    </div>
                                                    <div>
                                                        <div style="font-weight: 600;"><c:out value="${u.name}" /></div>
                                                        <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${u.email}" /></div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.role == 'ADMIN'}">
                                                        <span class="chip status-peach">Admin</span>
                                                    </c:when>
                                                    <c:when test="${u.role == 'DOCTOR'}">
                                                        <span class="chip status-info">Doctor</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="chip status-success">Patient</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.role == 'DOCTOR'}">
                                                        <div style="font-size: 0.82rem; font-weight: 500;">
                                                            <c:out value="${u.specialization}" /> • Rm <c:out value="${u.roomNumber}" />
                                                        </div>
                                                        <div style="font-size: 0.75rem; color: var(--text-muted);">
                                                            Fee: $<c:out value="${u.consultationFee}" />
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="font-size: 0.82rem; color: var(--text-muted);"><c:out value="${u.email}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.status == 'ACTIVE'}">
                                                        <span class="chip status-success">Active</span>
                                                    </c:when>
                                                    <c:when test="${u.status == 'LOCKED'}">
                                                        <span class="chip status-warning">Locked</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="chip status-danger">Inactive</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <span style="font-size: 0.82rem; color: var(--text-muted);"><c:out value="${u.createdAt}" /></span>
                                            </td>
                                            <td style="text-align: right;">
                                                <div style="display: inline-flex; gap: 8px;">
                                                    <c:if test="${u.id != sessionScope.USER.id}">
                                                        <!-- Status Toggle Form -->
                                                        <form action="${pageContext.request.contextPath}/admin/users/status" method="post" style="display:inline;">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="id" value="${u.id}">
                                                            <input type="hidden" name="status" value="${u.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
                                                            <button type="submit" class="btn btn-ghost btn-sm" title="Toggle status">
                                                                <c:out value="${u.status == 'ACTIVE' ? 'Deactivate' : 'Activate'}" />
                                                            </button>
                                                        </form>

                                                        <!-- Delete Form -->
                                                        <form action="${pageContext.request.contextPath}/admin/users/delete" method="post" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this user?');">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="id" value="${u.id}">
                                                            <button type="submit" class="btn btn-ghost btn-sm" style="color: var(--danger);" title="Delete user">
                                                                ✕
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 32px;">
                                            No users found matching current filters.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination-wrap">
                            <div>Showing page ${currentPage} of ${totalPages} (${totalCount} total records)</div>
                            <div class="pagination-links">
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <a href="${pageContext.request.contextPath}/admin/users?page=${p}&q=${query}&role=${roleFilter}"
                                       class="btn btn-sm ${p == currentPage ? 'btn-primary' : 'btn-secondary'}">
                                        ${p}
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </main>
        </div>
    </div>

    <!-- Modal: Add Doctor Clinician -->
    <div id="addDoctorModal" class="modal-backdrop" role="dialog" aria-modal="true">
        <div class="modal-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h3 style="font-size: 1.3rem;">Onboard New Doctor</h3>
                <button type="button" class="btn btn-ghost btn-sm modal-close">&times;</button>
            </div>

            <form action="${pageContext.request.contextPath}/admin/users/create" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}">

                <div class="form-group">
                    <label class="form-label">Full Name *</label>
                    <input type="text" name="name" class="form-control" placeholder="Dr. Sophia Patel" required>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Email Address *</label>
                        <input type="email" name="email" class="form-control" placeholder="spatel@carefirst.com" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Temporary Password *</label>
                        <input type="password" name="password" class="form-control" placeholder="Min 6 chars" required>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Specialization *</label>
                        <input type="text" name="specialization" class="form-control" placeholder="Cardiology / Neurology" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Experience (Years)</label>
                        <input type="number" name="experience" class="form-control" value="5" min="0">
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Consultation Fee ($)</label>
                        <input type="number" name="consultationFee" class="form-control" value="80" step="5" min="0">
                    </div>

                    <div class="form-group">
                        <label class="form-label">Room / Clinic Office</label>
                        <input type="text" name="roomNumber" class="form-control" placeholder="Suite 302">
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">Doctor Bio</label>
                    <textarea name="bio" class="form-control" rows="3" placeholder="Brief clinical summary and research background..."></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
                    <button type="button" class="btn btn-secondary modal-close">Cancel</button>
                    <button type="submit" class="btn btn-primary">Create Doctor Profile</button>
                </div>
            </form>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
