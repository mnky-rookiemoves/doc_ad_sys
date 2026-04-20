package servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dao.ActivityLogDAO;
import dao.PasswordResetDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.EmailUtil;

@WebServlet("/reset-password")
public class ResetPasswordServlet
        extends HttpServlet {

    private static final long serialVersionUID =
        1L;

    private final UserDAO          userDAO  =
        new UserDAO();
    private final PasswordResetDAO resetDAO =
        new PasswordResetDAO();
    private final ActivityLogDAO   logDAO   =
        new ActivityLogDAO();

    /* =========================
       GET — Show reset form
       ========================= */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String token =
            request.getParameter("token");

        if (token == null
                || token.trim().isEmpty()) {
            response.sendRedirect(
                request.getContextPath()
                + "/forgot-password.jsp");
            return;
        }

        // ✅ Validate token
        int userId =
            resetDAO.validateToken(token);

        if (userId == -1) {
            // ❌ Invalid or expired
            request.setAttribute("error",
                "This reset link is invalid "
                + "or has expired. "
                + "Please request a new one.");
            request.getRequestDispatcher(
                "/forgot-password.jsp")
                   .forward(request, response);
            return;
        }

        // ✅ Valid — show reset form
        request.setAttribute("token", token);
        request.getRequestDispatcher(
            "/reset-password.jsp")
               .forward(request, response);
    }

    /* =========================
       POST — Process new password
       ========================= */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String token =
            request.getParameter("token");
        String newPassword =
            request.getParameter("newPassword");
        String confirmPassword =
            request.getParameter("confirmPassword");

        /* =========================
           VALIDATE INPUTS
           ========================= */
        if (token == null
                || token.trim().isEmpty()
                || newPassword == null
                || newPassword.trim().isEmpty()) {

            request.setAttribute("error",
                "Invalid request.");
            request.getRequestDispatcher(
                "/reset-password.jsp")
                   .forward(request, response);
            return;
        }

        // ✅ Passwords must match
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("token", token);
            request.setAttribute("error",
                "Passwords do not match.");
            request.getRequestDispatcher(
                "/reset-password.jsp")
                   .forward(request, response);
            return;
        }

        // ✅ Minimum length
        if (newPassword.length() < 6) {
            request.setAttribute("token", token);
            request.setAttribute("error",
                "Password must be at least "
                + "6 characters.");
            request.getRequestDispatcher(
                "/reset-password.jsp")
                   .forward(request, response);
            return;
        }

        /* =========================
           VALIDATE TOKEN
           ========================= */
        int userId =
            resetDAO.validateToken(token);

        if (userId == -1) {
            request.setAttribute("error",
                "This reset link is invalid "
                + "or has expired.");
            request.getRequestDispatcher(
                "/forgot-password.jsp")
                   .forward(request, response);
            return;
        }

        /* =========================
           UPDATE PASSWORD
           ========================= */
        String hashed =
            UserDAO.hashPassword(
                newPassword.trim());

        boolean updated =
            userDAO.updatePassword(
                userId, hashed);

        if (updated) {

            // ✅ Mark token as used
            resetDAO.markUsed(token);

            // ✅ Cleanup old tokens
            resetDAO.cleanupExpired();

            // ✅ Fetch user for email
            User user =
                userDAO.getUserById(userId);

            // ✅ Log it
            logDAO.log(
                userId,
                user != null
                    ? user.getUsername()
                    : "Unknown",
                "UPDATE",
                "Users",
                "Password reset via "
                + "forgot password link"
            );

            // ✅ Send confirmation email
            try {
                if (user != null
                        && user.getEmail() != null
                        && !user.getEmail()
                            .isEmpty()) {

                    SimpleDateFormat dtf =
                        new SimpleDateFormat(
                            "MMM dd, yyyy hh:mm a");

                    EmailUtil.sendPasswordChanged(
                        user.getEmail(),
                        user.getUsername(),
                        dtf.format(new Date()),
                        request.getRemoteAddr());
                }
            } catch (Exception emailEx) {
                System.err.println(
                    "[EMAIL] ⚠️ Reset confirm: "
                    + emailEx.getMessage());
            }

            // ✅ Redirect to login
            response.sendRedirect(
                request.getContextPath()
                + "/login.jsp?success="
                + "Password+reset+successfully."
                + "+You+can+now+login.");

        } else {
            request.setAttribute("token", token);
            request.setAttribute("error",
                "Failed to reset password. "
                + "Please try again.");
            request.getRequestDispatcher(
                "/reset-password.jsp")
                   .forward(request, response);
        }
    }
}