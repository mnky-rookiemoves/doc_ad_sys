<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    boolean isAdmin = currentUser.getRole() != null && "admin".equalsIgnoreCase(currentUser.getRole());
    if (!isAdmin) {
        response.sendRedirect(request.getContextPath() + "/home.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Requirements - VANDAM</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f4f4f4; }
        .container {
            width: 1000px; max-width: 95%;
            margin: 30px auto; background: #fff;
            padding: 25px; border-radius: 10px;
            box-shadow: 0 0 10px #ccc;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Document Requirements Management</h2>
        <p>Coming soon...</p>
        <a href="${pageContext.request.contextPath}/home.jsp">← Back to Dashboard</a>
    </div>
</body>
</html>