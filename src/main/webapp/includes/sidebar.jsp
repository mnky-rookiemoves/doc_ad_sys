<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="model.User" %>

<%
    String _uri = request.getRequestURI();
    User _u = (User) session
        .getAttribute("currentUser");

    // ✅ Pre-build all hrefs and classes
    String ctx = request.getContextPath();

    String classDash  = _uri.contains("/dashboard")
        ? "active" : "";
    String classVerify = _uri.contains("/verify")
        ? "active" : "";
    String classStud  = _uri.contains("/students")
        ? "active" : "";
    String classCat   = _uri.contains("/categories")
        ? "active" : "";
    String classReq   = _uri.contains("/requirements")
        ? "active" : "";
    String classLog   = _uri.contains("/activity-log")
        ? "active" : "";
    String classUser  = _uri.contains("/user-management")
        ? "active" : "";
    String classProf  = _uri.contains("/profile")
        ? "active" : "";
    String classSet   = _uri.contains("/settings")
        ? "active" : "";
%>

<div class="sidebar">
    <h2>VANDAM</h2>

    <a href="<%= ctx %>/dashboard"
       class="<%= classDash %>">
        <i class="fas fa-tachometer-alt"></i>
        Dashboard
    </a>

    <!-- VERIFY DOCUMENT -->
    <a href="<%= ctx %>/verify"
    class="<%= _uri.contains("/verify")
        ? "active" : "" %>">
        <i class="fas fa-search"></i>
        Verify Document
    </a>

    <a href="<%= ctx %>/students"
       class="<%= classStud %>">
        <i class="fas fa-users"></i>
        Students
    </a>

    <a href="<%= ctx %>/categories"
       class="<%= classCat %>">
        <i class="fas fa-list"></i>
        Categories
    </a>

    <a href="<%= ctx %>/requirements"
       class="<%= classReq %>">
        <i class="fas fa-file-alt"></i>
        Requirements
    </a>

    <a href="<%= ctx %>/activity-log"
       class="<%= classLog %>">
        <i class="fas fa-history"></i>
        Activity Log
    </a>

    <% if (_u != null && _u.isSuperAdmin()) { %>
    <a href="<%= ctx %>/user-management"
       class="<%= classUser %>">
        <i class="fas fa-user-cog"></i>
        Users
    </a>
    <% } %>

    <a href="<%= ctx %>/profile"
       class="<%= classProf %>">
        <i class="fas fa-user-circle"></i>
        My Profile
    </a>

    <a href="<%= ctx %>/settings"
       class="<%= classSet %>">
        <i class="fas fa-paint-brush"></i>
        Settings
    </a>

    <a href="<%= ctx %>/logout"
       class="logout">
        <i class="fas fa-sign-out-alt"></i>
        Logout
    </a>

</div>