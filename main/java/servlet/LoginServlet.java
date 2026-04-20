package servlet;

import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UserDAO dao = new UserDAO();
        User user = dao.authenticateUser(username, password);

        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);

            // ✅ HARD STOP AFTER REDIRECT
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // ❌ ONLY reached if login fails
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Invalid");
    }
}