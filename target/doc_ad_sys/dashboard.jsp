<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - VANDAM</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .container { max-width: 1000px; margin: 0 auto; }
        .welcome-card {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            text-align: center;
            margin-bottom: 30px;
        }
        .welcome-card h1 { 
            color: #333; 
            margin-bottom: 10px; 
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 15px;
            font-size: 32px;
        }
        .welcome-card p { 
            color: #666; 
            font-size: 16px; 
            margin-bottom: 30px; 
        }
        .btn { 
            padding: 12px 30px; 
            background: #667eea; 
            border: none; 
            color: white; 
            border-radius: 6px; 
            cursor: pointer; 
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            margin: 10px;
            transition: all 0.3s ease;
            font-weight: 600;
        }
        .btn:hover { 
            background: #5568d3; 
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
        .btn-secondary { 
            background: #6c757d; 
        }
        .btn-secondary:hover { 
            background: #5a6268; 
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="welcome-card">
            <h1><i class="fas fa-tachometer-alt"></i> VANDAM Dashboard</h1>
            <p>Welcome, <strong><%= currentUser.getUsername() %></strong>! (Role: <strong><%= currentUser.getRole() %></strong>)</p>
            
            <div>
                <a href="${pageContext.request.contextPath}/students" class="btn">
                    <i class="fas fa-users"></i> Manage Students
                </a>
                
                <a href="${pageContext.request.contextPath}/categories" class="btn">
                    <i class="fas fa-list"></i> Categories
                </a>
                
                <a href="${pageContext.request.contextPath}/requirements" class="btn">
                    <i class="fas fa-file-alt"></i> Requirements
                </a>
                
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </a>
            </div>
        </div>
    </div>
</body>
</html>