<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, model.User" %>
<%@ include file="/includes/head.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    if (request.getAttribute("totalStudents") == null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }

    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    boolean isAdmin = currentUser.getRole() != null &&
            "admin".equalsIgnoreCase(currentUser.getRole());

    // ===== GET DASHBOARD DATA =====
    Integer totalStudents = (Integer) request.getAttribute("totalStudents");
    if (totalStudents == null) totalStudents = 0;

    Integer incompleteRequirements = (Integer) request.getAttribute("incompleteRequirements");
    if (incompleteRequirements == null) incompleteRequirements = 0;

    Integer completeRequirements = (Integer) request.getAttribute("completeRequirements");
    if (completeRequirements == null) completeRequirements = 0;

    Map<String, Integer> categoryBreakdown = (Map<String, Integer>) request.getAttribute("categoryBreakdown");
    if (categoryBreakdown == null) categoryBreakdown = new HashMap<>();

    String _theme =
        (String) session.getAttribute("theme");
    if (_theme == null) _theme = "normal";

    String successMsg = request.getParameter("success");
    String errorMsg   = request.getParameter("error");

%>

<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet"
      href="<%= request.getContextPath()
      %>/css/themes.css">
<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <meta charset="UTF-8">
    <title>Dashboard - VANDAM</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: 'Segoe UI', Tahoma, sans-serif;
            background: #f4f4f4;
            display: flex;
            min-height: 100vh;
        }

        /* SIDEBAR */
        .sidebar {
            width: 250px;
            background: #6d0f0f;
            color: #fff;
            display: flex;
            flex-direction: column;
        }

        .sidebar h2 {
            padding: 20px;
            text-align: center;
            background: #5a0c0c;
            font-size: 20px;
        }

        .sidebar a {
            padding: 15px 20px;
            color: #fff;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 12px;
            transition: background 0.3s;
        }

        .sidebar a:hover {
            background: #8b1a1a;
        }

        .sidebar a.active {
            background: #8b1a1a;
            font-weight: 600;
        }

        .sidebar .logout {
            margin-top: auto;
            background: #4a0a0a;
        }

        .sidebar .logout:hover {
            background: #6d0f0f;
        }

        /* MAIN CONTENT */
        .main {
            flex: 1;
            padding: 30px;
            overflow-y: auto;
        }

        .header {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .header h1 {
            color: #6d0f0f;
            font-size: 26px;
        }

        .user-info {
            font-size: 14px;
            color: #555;
            text-align: right;
        }

        /* DASHBOARD CARDS */
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .card {
            background: white;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            text-align: center;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .card:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(0,0,0,0.15);
        }

        .card i {
            font-size: 40px;
            color: #6d0f0f;
            margin-bottom: 10px;
        }

        .card h3 {
            margin-bottom: 5px;
            color: #333;
        }

        .card .number {
            font-size: 32px;
            font-weight: bold;
            color: #6d0f0f;
            margin: 10px 0;
        }

        .card p {
            color: #777;
            font-size: 13px;
        }

        /* STATUS CARDS */
        .card.success {
            border-left: 5px solid #2e7d32;
        }

        .card.success i {
            color: #2e7d32;
        }

        .card.success .number {
            color: #2e7d32;
        }

        .card.warning {
            border-left: 5px solid #f57c00;
        }

        .card.warning i {
            color: #f57c00;
        }

        .card.warning .number {
            color: #f57c00;
        }

        /* BREAKDOWN TABLE */
        .breakdown-section {
            background: white;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }

        .breakdown-section h2 {
            color: #6d0f0f;
            margin-bottom: 20px;
            font-size: 18px;
            border-bottom: 2px solid #f0f0f0;
            padding-bottom: 10px;
        }

        .breakdown-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 15px;
        }

        .breakdown-item {
            padding: 15px;
            background: #f9f9f9;
            border-radius: 6px;
            border-left: 4px solid #6d0f0f;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .breakdown-item span {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .breakdown-item .badge {
            background: #6d0f0f;
            color: white;
            padding: 5px 12px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 14px;
        }

        .breakdown-item-label {
            color: #555;
            font-size: 14px;
        }

        /* EMPTY STATE */
        .empty-state {
            text-align: center;
            padding: 30px;
            color: #999;
        }

        .empty-state i {
            font-size: 48px;
            color: #ddd;
            margin-bottom: 15px;
        }
    </style>
</head>

<body class="theme-<%= _theme %>">
    
<!-- SIDEBAR -->
<jsp:include page="/includes/sidebar.jsp" />


<!-- MAIN CONTENT -->
<div class="main">

    <% if (totalStudents == 0 && categoryBreakdown.isEmpty()) { %>
    <div class="empty-state">
        <p>Loading dashboard data...</p>
    </div>
    <% } %>

    <div class="header">

    <!-- LEFT: Title -->
    <h1>Dashboard</h1>

    <!-- CENTER: Print All Reports Button -->
    <a href="<%=request.getContextPath()%>/dashboard-report"
   style="
       padding: 8px 18px;
       background: #6d0f0f;
       color: #fff;
       border-radius: 4px;
       text-decoration: none;
       font-size: 13px;
       font-weight: bold;
       display: inline-block;">
    🖨 Print All Reports
    </a>

    <%-- ✅ EMAIL TEST BUTTON — remove after testing --%>
        <div class="breakdown-section no-print"
            style="border:2px dashed #f9a825;
                    background:#fff8e1;">
            <h2 style="color:#f57c00;">
                🧪 Email Test (Remove after testing)
            </h2>
            <p style="font-size:13px; color:#777;
                    margin-bottom:16px;">
                Click to send a test email and verify
                Gmail SMTP is working correctly.
            </p>
            <form method="POST"
                action="<%= request.getContextPath()
                    %>/test-email">
                <input type="text"
                    name="toEmail"
                    placeholder="Send test to: you@gmail.com"
                    style="
                        padding:8px 12px;
                        border:1px solid #ccc;
                        border-radius:4px;
                        font-size:13px;
                        width:280px;
                        margin-right:10px;">
                <button type="submit"
                        style="
                            padding:8px 18px;
                            background:#f57c00;
                            color:#fff;
                            border:none;
                            border-radius:4px;
                            cursor:pointer;
                            font-size:13px;
                            font-weight:bold;">
                    📧 Send Test Email
                </button>
            </form>

            <%-- Show result --%>
            <% String emailResult = (String)
                request.getAttribute("emailResult");
            if (emailResult != null) { %>
            <p style="margin-top:12px;
                    font-weight:bold;
                    color:<%= emailResult.startsWith("✅")
                        ? "#2e7d32" : "#c62828" %>;">
                <%= emailResult %>
            </p>
            
            <% if (successMsg != null) { %>
            <div style="
                background:#e8f5e9;
                border:1px solid #2e7d32;
                color:#2e7d32;
                padding:12px 16px;
                border-radius:6px;
                margin-bottom:16px;
                font-size:13px;
                font-weight:bold;">
                ✅ <%= successMsg %>
            </div>
            <% } %>

            <% if (errorMsg != null) { %>
            <div style="
                background:#ffebee;
                border:1px solid #c62828;
                color:#c62828;
                padding:12px 16px;
                border-radius:6px;
                margin-bottom:16px;
                font-size:13px;
                font-weight:bold;">
                ❌ <%= errorMsg %>
            </div>
            <% } %>

            <% } %>
        </div>

    <!-- RIGHT: User Info -->
    <div class="user-info">
        Logged in as
        <strong><%= currentUser.getUsername() %></strong><br>
        Role:
        <strong><%= currentUser.getRole() %></strong>
    </div>
    
</div>

    <!-- KEY STATISTICS CARDS -->
    <div class="cards-grid">

        <div class="card">
            <i class="fas fa-users"></i>
            <h3>Total Students</h3>
            <div class="number"><%= totalStudents %></div>
            <p>Active students in system</p>
        </div>

        <div class="card success">
            <i class="fas fa-check-circle"></i>
            <h3>Requirements Complete</h3>
            <div class="number"><%= completeRequirements %></div>
            <p>Students with all submissions</p>
        </div>

        <div class="card warning">
            <i class="fas fa-exclamation-circle"></i>
            <h3>Pending Requirements</h3>
            <div class="number"><%= incompleteRequirements %></div>
            <p>Students needing attention</p>
        </div>

        <div class="card">
            <i class="fas fa-list"></i>
            <h3>Categories</h3>
            <div class="number"><%= categoryBreakdown.size() %></div>
            <p>Student classifications</p>
        </div>

    </div>

    <!-- CATEGORY BREAKDOWN -->
    <div class="breakdown-section">
        <h2>📊 Students by Category</h2>
        
        <% if (categoryBreakdown.isEmpty()) { %>
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>No categories yet</p>
            </div>
        <% } else { %>
            <div class="breakdown-list">
                <% for (Map.Entry<String, Integer> entry : categoryBreakdown.entrySet()) { %>
                    <div class="breakdown-item">
                        <span>
                            <i class="fas fa-graduation-cap"></i>
                            <span class="breakdown-item-label"><%= entry.getKey() %></span>
                        </span>
                        <span class="badge"><%= entry.getValue() %></span>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>

    <!-- QUICK SUMMARY -->
    <div class="breakdown-section">
        <h2>📈 Quick Summary</h2>
        <div class="breakdown-list">
            <div class="breakdown-item">
                <span>
                    <i class="fas fa-user-check"></i>
                    <span class="breakdown-item-label">Completion Rate</span>
                </span>
                <span class="badge">
                    <%= totalStudents > 0 ? Math.round((completeRequirements * 100.0) / totalStudents) : 0 %>%
                </span>
            </div>
            <div class="breakdown-item">
                <span>
                    <i class="fas fa-hourglass-half"></i>
                    <span class="breakdown-item-label">Pending Completion</span>
                </span>
                <span class="badge"><%= incompleteRequirements %> of <%= totalStudents %></span>
            </div>
        </div>
    </div>

</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>