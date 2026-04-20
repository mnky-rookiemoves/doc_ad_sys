<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/includes/head.jsp" %>
<%
     // If user is already logged in, redirect to dashboard
     if (session.getAttribute("user") != null) {
         response.sendRedirect(request.getContextPath() + "/dashboard");
         return;
     }

%>
<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">
    <title>Login - VANDAM Document Admission System</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Font Awesome -->
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Segoe UI', Tahoma, sans-serif;
            background: linear-gradient(135deg, #6d0f0f, #3d0707);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .login-container {
            background: white;
            width: 380px;
            padding: 35px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.25);
        }

        .login-header {
            text-align: center;
            margin-bottom: 25px;
        }

        .login-header h1 {
            color: #6d0f0f;
            font-size: 28px;
            margin-bottom: 5px;
        }

        .login-header p {
            font-size: 13px;
            color: #777;
        }

        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-size: 14px;
            font-weight: 600;
            color: #333;
        }

        .form-group input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
        }

        .form-group input:focus {
            outline: none;
            border-color: #6d0f0f;
            box-shadow: 0 0 0 2px rgba(109, 15, 15, 0.15);
        }

        .btn-login {
            width: 100%;
            padding: 12px;
            background: #6d0f0f;
            border: none;
            border-radius: 6px;
            color: white;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
        }

        .btn-login:hover {
            background: #8b1a1a;
        }

        .error-msg {
            background: #fdecea;
            border-left: 5px solid #c62828;
            color: #c62828;
            padding: 10px;
            margin-bottom: 15px;
            font-size: 13px;
        }

        .footer {
            margin-top: 20px;
            text-align: center;
            font-size: 12px;
            color: #777;
        }
    </style>
</head>

<body>

<div class="login-container">

    <div class="login-header">
        <h1>VANDAM</h1>
        <p>Document Admission System</p>
    </div>

    <% if (request.getParameter("error") != null) { %>
        <div class="error-msg">
            <i class="fas fa-exclamation-circle"></i>
            Invalid username or password
        </div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/login">

        <div class="form-group">
            <label><i class="fas fa-user"></i> Username</label>
            <input type="text" name="username" required autofocus>
        </div>

        <div class="form-group">
            <label><i class="fas fa-lock"></i> Password</label>
            <input type="password" name="password" required>
        </div>

        <button type="submit" class="btn-login">
            <i class="fas fa-sign-in-alt"></i> Login
        </button>
    </form>
            <%-- ✅ Forgot Password link --%>
        <div style="text-align:center;
            margin-top:12px;
            font-size:13px;">
            <a href="<%= request.getContextPath()
                %>/forgot-password"
            style="color:#6d0f0f;
                    text-decoration:none;">
                🔑 Forgot Password?
            </a>
        </div>

    <div class="footer">
        &copy; 2026 VANDAM | PUPOUS-BSITOUMN 2-3
    </div>

</div>

</body>
</html>
