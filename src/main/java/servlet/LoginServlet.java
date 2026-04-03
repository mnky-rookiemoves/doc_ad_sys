package servlet;

import dao.UserDAO;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to login.jsp
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validation
        if (username == null || username.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Username is required");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Password is required");
            return;
        }

        try {
            // Authenticate user
            UserDAO userDAO = new UserDAO();
            User user = userDAO.authenticateUser(username.trim(), password);

            if (user != null) {
                // Login successful
                HttpSession session = request.getSession();
                session.setAttribute("currentUser", user);
                System.out.println("✓ User logged in: " + username);
                
                // Redirect to students page
                response.sendRedirect(request.getContextPath() + "/students");
            } else {
                // Login failed
                System.out.println("✗ Login failed for user: " + username);
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=Invalid username or password");
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=An error occurred during login");
        }
    }
}