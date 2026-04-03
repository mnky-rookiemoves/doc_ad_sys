<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Student" %>
<%@ page import="model.User" %>
<%@ page import="model.StudentCategory" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    @SuppressWarnings("unchecked")
    List<Student> studentList = (List<Student>) request.getAttribute("studentList");
    
    @SuppressWarnings("unchecked")
    List<StudentCategory> categoryList = (List<StudentCategory>) request.getAttribute("categoryList");

    String errorMsg = (String) request.getAttribute("error");
    String successMsg = (String) request.getAttribute("success");
    Student editStudent = (Student) request.getAttribute("editStudent");
    
    boolean isAdmin = currentUser.getRole() != null && "admin".equalsIgnoreCase(currentUser.getRole());
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Students - VANDAM Document Admission System</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { 
            margin: 0; 
            padding: 0; 
            box-sizing: border-box; 
        }
        
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px 0;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        .header {
            background: white;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            color: #333;
            font-size: 28px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .header-right {
            display: flex;
            gap: 20px;
            align-items: center;
        }
        
        .user-info {
            color: #666;
            font-size: 14px;
            text-align: right;
        }
        
        .user-info strong {
            color: #667eea;
        }
        
        .nav-buttons {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        
        .nav-buttons a {
            text-decoration: none;
        }
        
        .btn {
            padding: 10px 15px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            text-decoration: none;
            color: white;
        }
        
        .btn-primary {
            background: #667eea;
        }
        
        .btn-primary:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
        
        .btn-secondary {
            background: #6c757d;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
            transform: translateY(-2px);
        }
        
        .notification-banner {
            background: white;
            border-left: 5px solid #4caf50;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .notification-banner i {
            font-size: 32px;
            color: #4caf50;
            min-width: 40px;
        }

        .notification-content {
            flex: 1;
        }

        .notification-content h3 {
            color: #333;
            margin-bottom: 5px;
            font-size: 16px;
            font-weight: 600;
        }

        .notification-content p {
            color: #666;
            font-size: 13px;
        }

        .notification-stats {
            display: flex;
            gap: 20px;
        }

        .stat-item {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .stat-number {
            font-size: 24px;
            font-weight: bold;
            color: #667eea;
        }

        .stat-label {
            font-size: 12px;
            color: #999;
            margin-top: 4px;
        }
        
        .msg-error { 
            background: #ffebee;
            border-left: 5px solid #c62828;
            color: #c62828;
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .msg-success { 
            background: #e8f5e9;
            border-left: 5px solid #2e7d32;
            color: #2e7d32;
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .content-area {
            background: white;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        
        .section {
            margin-bottom: 40px;
        }
        
        .section-title {
            font-size: 20px;
            color: #333;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 3px solid #667eea;
            display: inline-block;
        }
        
        .form-section {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
            border: 1px solid #e9ecef;
        }
        
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin-bottom: 15px;
        }
        
        @media (max-width: 768px) {
            .form-row {
                grid-template-columns: 1fr;
            }
        }
        
        .form-group {
            display: flex;
            flex-direction: column;
        }
        
        .form-group label {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
        }
        
        .form-group input,
        .form-group select {
            padding: 10px 15px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
            font-family: inherit;
        }
        
        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .required::after {
            content: " *";
            color: #c62828;
        }
        
        .search-filter {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
            border: 1px solid #e9ecef;
        }
        
        .filter-row {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr auto;
            gap: 15px;
            margin-bottom: 15px;
        }
        
        @media (max-width: 768px) {
            .filter-row {
                grid-template-columns: 1fr;
            }
        }
        
        .filter-group {
            display: flex;
            flex-direction: column;
        }
        
        .filter-group label {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 5px;
        }
        
        .filter-group input,
        .filter-group select {
            padding: 10px 15px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            align-items: flex-end;
        }
        
        .table-wrapper {
            overflow-x: auto;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        
        th {
            background: #f8f9fa;
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: #333;
            border-bottom: 2px solid #ddd;
            font-size: 14px;
        }
        
        td {
            padding: 15px;
            border-bottom: 1px solid #eee;
            font-size: 14px;
        }
        
        tr:hover {
            background: #f8f9fa;
        }
        
        .student-name {
            color: #667eea;
            font-weight: 600;
        }
        
        .actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }
        
        .action-btn {
            padding: 6px 12px;
            font-size: 12px;
            border-radius: 4px;
            cursor: pointer;
            border: none;
            color: white;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        
        .action-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        }
        
        .action-btn-view {
            background: #29b6f6;
        }
        
        .action-btn-view:hover {
            background: #0288d1;
        }
        
        .action-btn-edit {
            background: #66bb6a;
        }
        
        .action-btn-edit:hover {
            background: #43a047;
        }
        
        .action-btn-delete {
            background: #ef5350;
        }
        
        .action-btn-delete:hover {
            background: #e53935;
        }
        
        .no-data {
            text-align: center;
            padding: 40px;
            color: #999;
            font-size: 16px;
        }
        
        .no-data i {
            font-size: 48px;
            color: #ddd;
            margin-bottom: 15px;
            display: block;
        }
        
        .edit-form-container {
            background: #fff3cd;
            border-left: 5px solid #ffc107;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        
        .edit-form-container h3 {
            color: #856404;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
    </style>
</head>
<body>

<div class="container">
    
    <!-- HEADER WITH NAVIGATION -->
    <div class="header">
        <div>
            <h1><i class="fas fa-users"></i> Students Management</h1>
        </div>
        <div class="header-right">
            <div class="user-info">
                <i class="fas fa-user-circle"></i> Logged in as: <strong><%= currentUser.getUsername() %></strong> 
                <br>Role: <strong><%= currentUser.getRole() %></strong>
            </div>
            <div class="nav-buttons">
                <a href="${pageContext.request.contextPath}/dashboard.jsp" class="btn btn-primary">
                    <i class="fas fa-home"></i> Home
                </a>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </a>
            </div>
        </div>
    </div>
    
    <!-- NOTIFICATION BANNER -->
    <div class="notification-banner">
        <i class="fas fa-bell"></i>
        <div class="notification-content">
            <h3><i class="fas fa-clipboard-check"></i> Students Overview</h3>
            <p>Manage and track all student information and their document submissions</p>
        </div>
        <div class="notification-stats">
            <div class="stat-item">
                <span class="stat-number"><%= (studentList != null ? studentList.size() : 0) %></span>
                <span class="stat-label">Total Students</span>
            </div>
        </div>
    </div>
    
    <!-- ERROR/SUCCESS MESSAGES -->
    <% if (errorMsg != null) { %>
        <div class="msg-error">
            <i class="fas fa-times-circle"></i>
            <%= errorMsg %>
        </div>
    <% } %>
    
    <% if (successMsg != null) { %>
        <div class="msg-success">
            <i class="fas fa-check-circle"></i>
            <%= successMsg %>
        </div>
    <% } %>
    
    <!-- MAIN CONTENT -->
    <div class="content-area">
        
        <!-- EDIT STUDENT SECTION (IF EDITING) -->
        <% if (editStudent != null && isAdmin) { %>
        <div class="edit-form-container">
            <h3><i class="fas fa-edit"></i> Edit Student Information</h3>
            
            <form method="post" action="${pageContext.request.contextPath}/students">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="studentId" value="<%= editStudent.getStudentId() %>">
                
                <div class="form-row">
                    <div class="form-group">
                        <label class="required">Student Name</label>
                        <input type="text" name="studentName" value="<%= editStudent.getStudentName() %>" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Birth Date</label>
                        <input type="date" name="birthDate" value="<%= (editStudent.getBirthDate() != null ? editStudent.getBirthDate() : "") %>">
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label class="required">Email</label>
                        <input type="email" name="email" value="<%= editStudent.getEmail() %>" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Category</label>
                        <select name="categoryId">
                            <option value="">-- Select Category --</option>
                            <% if (categoryList != null) {
                                for (StudentCategory c : categoryList) {
                                    String selected = (editStudent.getCategoryId() != null && editStudent.getCategoryId().equals(c.getCategoryId())) ? "selected" : "";
                            %>
                                <option value="<%= c.getCategoryId() %>" <%= selected %>><%= c.getCategoryName() %></option>
                            <% }} %>
                        </select>
                    </div>
                </div>
                
                <div class="form-row">
                    <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Update Student</button>
                    <a href="${pageContext.request.contextPath}/students" class="btn btn-secondary"><i class="fas fa-times"></i> Cancel</a>
                </div>
            </form>
        </div>
        <% } %>
        
        <!-- ADD STUDENT SECTION (ADMIN ONLY) -->
        <% if (isAdmin && editStudent == null) { %>
        <div class="section">
            <h2 class="section-title"><i class="fas fa-user-plus"></i> Add New Student</h2>
            
            <div class="form-section">
                <form method="post" action="${pageContext.request.contextPath}/students">
                    <input type="hidden" name="action" value="create">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label class="required">Student Name</label>
                            <input type="text" name="studentName" required placeholder="Enter full name">
                        </div>
                        
                        <div class="form-group">
                            <label>Birth Date</label>
                            <input type="date" name="birthDate">
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label class="required">Email</label>
                            <input type="email" name="email" required placeholder="student@example.com">
                        </div>
                        
                        <div class="form-group">
                            <label>Category</label>
                            <select name="categoryId">
                                <option value="">-- Select Category --</option>
                                <% if (categoryList != null) {
                                    for (StudentCategory c : categoryList) { %>
                                        <option value="<%= c.getCategoryId() %>"><%= c.getCategoryName() %></option>
                                <% }} %>
                            </select>
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Save Student</button>
                    </div>
                </form>
            </div>
        </div>
        <% } %>
        
        <!-- SEARCH & FILTER SECTION -->
        <div class="section">
            <h2 class="section-title"><i class="fas fa-search"></i> Search & Filter Students</h2>
            
            <div class="search-filter">
                <form method="get" action="${pageContext.request.contextPath}/students">
                    <div class="filter-row">
                        <div class="filter-group">
                            <label>Search by Name or Email</label>
                            <input type="text" name="search" placeholder="Enter name or email">
                        </div>
                        
                        <div class="filter-group">
                            <label>Category</label>
                            <select name="categoryId">
                                <option value="">-- All Categories --</option>
                                <% if (categoryList != null) {
                                    for (StudentCategory c : categoryList) { %>
                                        <option value="<%= c.getCategoryId() %>">
                                            <%= c.getCategoryName() %>
                                        </option>
                                <% }} %>
                            </select>
                        </div>

                        <div class="filter-group">
                            <label>&nbsp;</label>
                        </div>
                        
                        <div class="button-group">
                            <button type="submit" class="btn btn-primary"><i class="fas fa-filter"></i> Search</button>
                            <a href="${pageContext.request.contextPath}/students" style="text-decoration: none;">
                                <button type="button" class="btn btn-secondary"><i class="fas fa-redo"></i> Reset</button>
                            </a>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- STUDENT LIST SECTION -->
        <div class="section">
            <h2 class="section-title"><i class="fas fa-list"></i> Student List</h2>
            
            <div class="table-wrapper">
                <% if (studentList == null || studentList.isEmpty()) { %>
                    <div class="no-data">
                        <i class="fas fa-inbox"></i>
                        <p>No students found.</p>
                    </div>
                <% } else { %>
                    <table>
                        <thead>
                            <tr>
                                <th><i class="fas fa-hashtag"></i> ID</th>
                                <th><i class="fas fa-user"></i> Name</th>
                                <th><i class="fas fa-birthday-cake"></i> Birth Date</th>
                                <th><i class="fas fa-envelope"></i> Email</th>
                                <th><i class="fas fa-tag"></i> Category</th>
                                <th><i class="fas fa-cog"></i> Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Student s : studentList) { %>
                                <tr>
                                    <td><strong><%= s.getStudentId() %></strong></td>
                                    <td class="student-name"><%= s.getStudentName() %></td>
                                    <td><%= (s.getBirthDate() != null && !s.getBirthDate().isEmpty() ? s.getBirthDate() : "-") %></td>
                                    <td><%= (s.getEmail() != null ? s.getEmail() : "-") %></td>
                                    <td><%= (s.getCategoryName() != null ? s.getCategoryName() : "-") %></td>
                                    <td>
                                        <div class="actions">
                                            <button class="action-btn action-btn-view" onclick="viewStudent(<%= s.getStudentId() %>)">
                                                <i class="fas fa-eye"></i> View
                                            </button>
                                            <% if (isAdmin) { %>
                                                <a href="${pageContext.request.contextPath}/students?action=edit&id=<%= s.getStudentId() %>" style="text-decoration: none;">
                                                    <button class="action-btn action-btn-edit" type="button">
                                                        <i class="fas fa-edit"></i> Edit
                                                    </button>
                                                </a>
                                                <button class="action-btn action-btn-delete" onclick="deleteStudent(<%= s.getStudentId() %>)">
                                                    <i class="fas fa-trash"></i> Delete
                                                </button>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>
        </div>
    </div>
</div>

<script>
    function deleteStudent(studentId) {
        if (confirm('Are you sure you want to delete this student? This action cannot be undone.')) {
            window.location.href = '${pageContext.request.contextPath}/students?action=delete&studentId=' + studentId;
        }
    }
    
    function viewStudent(studentId) {
        alert('View Details - Feature coming soon!\nStudent ID: ' + studentId);
    }
</script>

</body>
</html>