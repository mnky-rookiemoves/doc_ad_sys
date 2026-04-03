<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    System.out.println("\n=== HOME.JSP DEBUG ===");
    System.out.println("Session ID: " + session.getId());
    System.out.println("CurrentUser from session: " + currentUser);
    
    if (currentUser == null) {
        System.out.println("ERROR: currentUser is NULL in session - redirecting to login");
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    System.out.println("✓ User logged in: " + currentUser.getUsername());
    System.out.println("  Role: " + currentUser.getRole());
    
    boolean isAdmin = currentUser.getRole() != null && "admin".equalsIgnoreCase(currentUser.getRole());
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VANDAM - Dashboard</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
        }

        .navbar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .navbar h1 {
            font-size: 24px;
        }

        .navbar-right {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .role-badge {
            background: rgba(255, 255, 255, 0.3);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .role-badge.admin {
            background: rgba(255, 107, 107, 0.7);
        }

        .role-badge.user {
            background: rgba(102, 180, 255, 0.7);
        }

        .navbar a {
            color: white;
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 4px;
            transition: background 0.3s;
            background: rgba(255, 255, 255, 0.2);
        }

        .navbar a:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .container {
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .welcome-card {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
            text-align: center;
            margin-bottom: 30px;
        }

        .welcome-card h2 {
            color: #333;
            margin-bottom: 10px;
        }

        .welcome-card p {
            color: #666;
            margin-bottom: 0;
        }

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
        }

        .dashboard-card {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
            text-align: center;
            cursor: pointer;
            transition: transform 0.3s, box-shadow 0.3s;
            text-decoration: none;
            color: inherit;
            display: block;
        }

        .dashboard-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
        }

        .dashboard-card .icon {
            font-size: 48px;
            margin-bottom: 15px;
        }

        .dashboard-card h3 {
            color: #333;
            margin-bottom: 10px;
        }

        .dashboard-card p {
            color: #666;
            font-size: 14px;
            margin-bottom: 0;
        }

        .dashboard-card-link {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 6px;
            transition: transform 0.2s;
            border: none;
            cursor: pointer;
        }

        .dashboard-card-link:hover {
            transform: scale(1.05);
        }

        .admin-only {
            opacity: 0.7;
        }

        .admin-only:hover {
            opacity: 1;
        }
    </style>
</head>
<body>
    <div class="navbar">
        <h1>🎓 VANDAM Dashboard</h1>
        <div class="navbar-right">
            <div class="user-info">
                <span><%= currentUser.getUsername() %></span>
                <span class="role-badge <%= isAdmin ? "admin" : "user" %>">
                    <%= currentUser.getRole().toUpperCase() %>
                </span>
            </div>
            <a href="${pageContext.request.contextPath}/logout">🚪 Logout</a>
        </div>
    </div>

    <div class="container">
        <div class="welcome-card">
            <h2>Welcome, <%= currentUser.getUsername() %>! 👋</h2>
            <p>Navigate to the sections below to manage your document admission process.</p>
        </div>

        <div class="dashboard-grid">
            <a href="${pageContext.request.contextPath}/students" class="dashboard-card">
                <div class="icon">👥</div>
                <h3>Students</h3>
                <p>Manage student records and information</p>
                <button class="dashboard-card-link">Go to Students →</button>
            </a>

            <a href="${pageContext.request.contextPath}/categories" class="dashboard-card <%= isAdmin ? "" : "admin-only" %>">
                <div class="icon">📂</div>
                <h3>Categories</h3>
                <p>Manage student categories</p>
                <button class="dashboard-card-link" <%= !isAdmin ? "disabled" : "" %>>Go to Categories →</button>
            </a>

            <a href="${pageContext.request.contextPath}/requirements" class="dashboard-card <%= isAdmin ? "" : "admin-only" %>">
                <div class="icon">📋</div>
                <h3>Requirements</h3>
                <p>Manage document requirements</p>
                <button class="dashboard-card-link" <%= !isAdmin ? "disabled" : "" %>>Go to Requirements →</button>
            </a>

            <a href="${pageContext.request.contextPath}/student-requirements" class="dashboard-card">
                <div class="icon">📄</div>
                <h3>Student Requirements</h3>
                <p>Track document submissions</p>
                <button class="dashboard-card-link">Go to Student Requirements →</button>
            </a>

            <% if (isAdmin) { %>
            <a href="${pageContext.request.contextPath}/profile.jsp" class="dashboard-card">
                <div class="icon">👤</div>
                <h3>My Profile</h3>
                <p>View and edit your profile</p>
                <button class="dashboard-card-link">Go to Profile →</button>
            </a>
            <% } %>
        </div>
    </div>
</body>
</html>