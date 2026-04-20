<%@ page contentType="text/html;
    charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/includes/head.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reset Password — VANDAM</title>
    <meta name="viewport"
          content="width=device-width,
          initial-scale=1.0">
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Segoe UI',
                Tahoma, sans-serif;
            /* ✅ Maroon background */
            background: #6d0f0f;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        .card {
            background: #fff;
            border-radius: 8px;
            padding: 40px;
            width: 420px;
            box-shadow: 0 8px 32px
                rgba(0, 0, 0, 0.3);
        }

        .header {
            text-align: center;
            margin-bottom: 28px;
        }

        .header .icon {
            font-size: 40px;
            margin-bottom: 10px;
        }

        .header h2 {
            color: #6d0f0f;
            font-size: 22px;
            margin-bottom: 8px;
        }

        .header p {
            color: #777;
            font-size: 13px;
        }

        .form-group {
            margin-bottom: 18px;
        }

        label {
            display: block;
            font-size: 13px;
            font-weight: bold;
            color: #333;
            margin-bottom: 6px;
        }

        input[type=password] {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 13px;
            transition: border 0.2s;
        }

        input[type=password]:focus {
            outline: none;
            border-color: #6d0f0f;
        }

        .btn-submit {
            width: 100%;
            padding: 12px;
            background: #6d0f0f;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.2s;
        }

        .btn-submit:hover {
            background: #5a0c0c;
        }

        .alert-error {
            background: #ffebee;
            border: 1px solid #c62828;
            color: #c62828;
            padding: 10px 14px;
            border-radius: 4px;
            font-size: 13px;
            margin-bottom: 16px;
            text-align: center;
        }

        .strength-bar {
            height: 6px;
            border-radius: 3px;
            background: #eee;
            margin-top: 6px;
            overflow: hidden;
        }

        .strength-fill {
            height: 100%;
            border-radius: 3px;
            width: 0%;
            transition: width 0.3s,
                background 0.3s;
        }

        .strength-label {
            font-size: 11px;
            color: #999;
            margin-top: 4px;
            text-align: right;
        }

        .back-link {
            text-align: center;
            margin-top: 16px;
            font-size: 13px;
        }

        .back-link a {
            color: #6d0f0f;
            text-decoration: none;
            font-weight: bold;
        }

        .back-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="card">

    <%-- HEADER --%>
    <div class="header">
        <div class="icon">🔑</div>
        <h2>Reset Password</h2>
        <p>Enter your new password below.</p>
    </div>

    <%-- ERROR MESSAGE --%>
    <%
        String error = (String)
            request.getAttribute("error");
        String token = (String)
            request.getAttribute("token");
    %>

    <% if (error != null) { %>
    <div class="alert-error">
        ❌ <%= error %>
    </div>
    <% } %>

    <%-- ✅ FIXED FORM — no stray URL text --%>
    <form action="<%= request.getContextPath()
        %>/reset-password"
          method="post">

        <%-- Hidden token --%>
        <input type="hidden"
               name="token"
               value="<%= token != null
                   ? token : "" %>">

        <%-- New Password --%>
        <div class="form-group">
            <label for="newPassword">
                New Password
            </label>
            <input type="password"
                   id="newPassword"
                   name="newPassword"
                   placeholder="Min. 6 characters"
                   required
                   autofocus
                   oninput="checkStrength(
                       this.value)">
            <div class="strength-bar">
                <div class="strength-fill"
                     id="strengthFill">
                </div>
            </div>
            <div class="strength-label"
                 id="strengthLabel">
            </div>
        </div>

        <%-- Confirm Password --%>
        <div class="form-group">
            <label for="confirmPassword">
                Confirm Password
            </label>
            <input type="password"
                   id="confirmPassword"
                   name="confirmPassword"
                   placeholder="Re-enter password"
                   required>
        </div>

        <%-- Submit Button --%>
        <button type="submit"
                class="btn-submit">
            ✅ Reset Password
        </button>

    </form>

    <%-- Back to Login --%>
    <div class="back-link">
        <a href="<%= request.getContextPath()
            %>/login.jsp">
            ← Back to Login
        </a>
    </div>

</div>

<script>
function checkStrength(val) {
    var fill =
        document.getElementById(
            'strengthFill');
    var label =
        document.getElementById(
            'strengthLabel');
    var len = val.length;

    if (len === 0) {
        fill.style.width = '0%';
        fill.style.background = '#eee';
        label.textContent = '';
    } else if (len < 6) {
        fill.style.width = '25%';
        fill.style.background = '#c62828';
        label.textContent = 'Too short';
    } else if (len < 10) {
        fill.style.width = '60%';
        fill.style.background = '#f57c00';
        label.textContent = 'Medium';
    } else {
        fill.style.width = '100%';
        fill.style.background = '#2e7d32';
        label.textContent = 'Strong ✅';
    }
}
</script>

</body>
</html>