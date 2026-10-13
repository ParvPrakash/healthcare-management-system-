<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<c:set var="pageTitle" value="User Account & Security Profile — CareFirst" scope="request"/>
<c:set var="activeNav" value="profile" scope="request"/>
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
                        <h1 class="page-title">Personal Profile & Security Settings</h1>
                        <p class="page-subtitle">Manage personal contact info, medical emergency numbers, and account credentials</p>
                    </div>
                </div>

                <div class="bento-grid">
                    <!-- General Details Form -->
                    <div class="bento-card col-span-7 reveal-card" style="--i: 0;">
                        <h3 style="margin-bottom: 18px; border-bottom: 1px solid var(--border); padding-bottom: 10px;">
                            👤 Personal Information
                        </h3>

                        <form action="${pageContext.request.contextPath}/profile" method="post">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                                <div class="form-group">
                                    <label class="form-label">Full Legal Name</label>
                                    <input type="text" name="name" class="form-control" value="<c:out value='${profileUser.name}' />" required>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Email Address</label>
                                    <input type="email" name="email" class="form-control" value="<c:out value='${profileUser.email}' />" required>
                                </div>
                            </div>

                            <c:if test="${profileUser.role == 'PATIENT'}">
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                                    <div class="form-group">
                                        <label class="form-label">Phone Number</label>
                                        <input type="tel" name="phone" class="form-control" value="<c:out value='${profileUser.phone}' />">
                                    </div>

                                    <div class="form-group">
                                        <label class="form-label">Blood Group</label>
                                        <input type="text" name="bloodGroup" class="form-control" value="<c:out value='${profileUser.bloodGroup}' />">
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Residential Address</label>
                                    <input type="text" name="address" class="form-control" value="<c:out value='${profileUser.address}' />">
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Emergency Contact Info</label>
                                    <input type="text" name="emergencyContact" class="form-control" value="<c:out value='${profileUser.emergencyContact}' />">
                                </div>
                            </c:if>

                            <c:if test="${profileUser.role == 'DOCTOR'}">
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                                    <div class="form-group">
                                        <label class="form-label">Specialization</label>
                                        <input type="text" name="specialization" class="form-control" value="<c:out value='${profileUser.specialization}' />">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Consultation Fee ($)</label>
                                        <input type="number" name="consultationFee" class="form-control" value="<c:out value='${profileUser.consultationFee}' />">
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Office / Room Number</label>
                                    <input type="text" name="roomNumber" class="form-control" value="<c:out value='${profileUser.roomNumber}' />">
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Professional Bio</label>
                                    <textarea name="bio" class="form-control" rows="3"><c:out value='${profileUser.bio}' /></textarea>
                                </div>
                            </c:if>

                            <button type="submit" class="btn btn-primary" style="margin-top: 12px; padding: 10px 24px;">
                                Save Profile Changes
                            </button>
                        </form>
                    </div>

                    <!-- Change Password Form -->
                    <div class="bento-card col-span-5 reveal-card" style="--i: 1;">
                        <h3 style="margin-bottom: 18px; border-bottom: 1px solid var(--border); padding-bottom: 10px;">
                            🔐 Change Password
                        </h3>

                        <form action="${pageContext.request.contextPath}/profile/password" method="post">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <div class="form-group">
                                <label class="form-label">Current Password *</label>
                                <input type="password" name="oldPassword" class="form-control" placeholder="••••••••" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label">New Password *</label>
                                <input type="password" name="newPassword" class="form-control" placeholder="Min 6 characters" required>
                            </div>

                            <button type="submit" class="btn btn-secondary" style="width: 100%; margin-top: 14px; padding: 10px;">
                                Update Password
                            </button>
                        </form>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/fragments/palette.jspf" %>
    <%@ include file="/WEB-INF/views/fragments/toast.jspf" %>
</body>
</html>
