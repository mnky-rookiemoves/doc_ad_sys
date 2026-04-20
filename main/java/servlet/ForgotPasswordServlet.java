package servlet;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import dao.PasswordResetDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.EmailUtil;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet
        extends HttpServlet {

    private static final long serialVersionUID
            = 1L;

    private final UserDAO userDAO
            = new UserDAO();
    private final PasswordResetDAO resetDAO
            = new PasswordResetDAO();

    /* =========================
       LOCAL IP
       ========================= */
    private static final String LOCAL_IP
            = "192.168.95.174";
    private static final String LOCAL_PORT
            = "8443";

    /* =========================
       GET — Show form
       ========================= */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/forgot-password.jsp")
                .forward(request, response);
    }

    /* =========================
       POST — Process request
       ========================= */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email
                = request.getParameter("email");

        if (email == null
                || email.trim().isEmpty()) {
            request.setAttribute("error",
                    "Please enter your email address.");
            request.getRequestDispatcher(
                    "/forgot-password.jsp")
                    .forward(request, response);
            return;
        }

        // ✅ Find user by email
        User user
                = userDAO.getUserByEmail(email.trim());

        // ✅ SECURITY: Always show success
        // even if email not found
        // Prevents email enumeration attack
        if (user != null) {

            // ✅ Generate secure random token
            String token = generateToken();

            // ✅ Expires in 30 minutes
            LocalDateTime expiresAt
                    = LocalDateTime.now().plusMinutes(30);

            // ✅ Save token to DB
            resetDAO.saveToken(
                    user.getUserId(),
                    token,
                    expiresAt);

            // ✅ Build reset URL
            String resetUrl
                    = "https://" + LOCAL_IP
                    + ":" + LOCAL_PORT
                    + request.getContextPath()
                    + "/reset-password?token="
                    + token;

            // ✅ Send email
            EmailUtil.sendPasswordReset(
                    email.trim(),
                    user.getUsername(),
                    resetUrl);
        }

        // ✅ Always show this message
        request.setAttribute("success",
                "If that email exists in our system, "
                + "a reset link has been sent.");
        request.getRequestDispatcher(
                "/forgot-password.jsp")
                .forward(request, response);
    }

    /* =========================
       GENERATE SECURE TOKEN
       ========================= */
    private String generateToken() {
        SecureRandom random
                = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
