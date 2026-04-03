<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.StudentCategory" %>
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

    @SuppressWarnings("unchecked")
    List<StudentCategory> categoryList = (List<StudentCategory>) request.getAttribute("categoryList");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Categories - VANDAM</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f4f4f4; }
        .container {
            width: 1000px; max-width: 95%;
            margin: 30px auto; background: #fff;
            padding: 25px; border-radius: 10px;
            box-shadow: 0 0 10px #ccc;
        }
        input, select { padding: 10px; width: 100%; border: 1px solid #ccc; border-radius: 6px; }
        .row { margin: 10px 0; }
        button { padding: 10px 14px; background: #1565c0; border: none; color: #fff; border-radius: 6px; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; margin-top: 18px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background: #f1f1f1; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Student Categories</h2>
        
        <h3>Add Category</h3>
        <form method="post" action="${pageContext.request.contextPath}/categories">
            <input type="hidden" name="action" value="add">
            <div class="row">
                <label>Category Name</label>
                <input type="text" name="categoryName" required>
            </div>
            <button type="submit">Add Category</button>
        </form>
        
        <h3 style="margin-top: 22px;">Categories List</h3>
        <% if (categoryList == null || categoryList.isEmpty()) { %>
            <p>No categories found.</p>
        <% } else { %>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Category Name</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (StudentCategory cat : categoryList) { %>
                        <tr>
                            <td><%= cat.getCategoryId() %></td>
                            <td><%= cat.getCategoryName() %></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/categories?action=delete&categoryId=<%= cat.getCategoryId() %>" onclick="return confirm('Are you sure?')">Delete</a>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </div>
</body>
</html>