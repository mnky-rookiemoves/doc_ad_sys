package servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.EmailUtil;

@WebServlet("/test-email")
public class TestEmailServlet
        extends HttpServlet {

    private static final long serialVersionUID
            = 1L;

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH CHECK
           ========================= */
        HttpSession session
                = request.getSession(false);
        User currentUser = (session != null)
                ? (User) session.getAttribute(
                        "currentUser")
                : null;

        if (currentUser == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp");
            return;
        }

        /* =========================
           ADMIN ONLY
           ========================= */
        if (!"admin".equalsIgnoreCase(
                currentUser.getRole())
                && !"superadmin".equalsIgnoreCase(
                        currentUser.getRole())) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/dashboard?error="
                    + "Access+denied.");
            return;
        }

        /* =========================
           GET TARGET EMAIL
           ========================= */
        String toEmail
                = request.getParameter("toEmail");

        if (toEmail == null
                || toEmail.trim().isEmpty()) {
            request.setAttribute(
                    "emailResult",
                    "❌ Please enter an email address.");
            request.getRequestDispatcher(
                    "/dashboard")
                    .forward(request, response);
            return;
        }

        /* =========================
           SEND ALL 4 TEST EMAILS
           ========================= */
        try {
            SimpleDateFormat dtf
                    = new SimpleDateFormat(
                            "MMM dd, yyyy hh:mm a");
            String now = dtf.format(new Date());

            /* =====================
               TEST 1
               File Uploaded
               ===================== */
            EmailUtil.sendFileUploaded(
                    toEmail,
                    "Juan dela Cruz (TEST)",
                    "NBI Clearance (TEST)",
                    currentUser.getUsername(),
                    now
            );

            /* =====================
               TEST 2
               Submission Complete
               ===================== */
            EmailUtil.sendSubmissionComplete(
                    toEmail,
                    "Juan dela Cruz (TEST)",
                    "VDM-TEST-00000-MN0-0000000",
                    7,
                    7,
                    "https://192.168.95.174:8443"
                    + request.getContextPath()
                    + "/verify?ref="
                    + "VDM-TEST-00000-MN0-0000000"
            );

            /* =====================
               TEST 3
               Account Created
               ===================== */
            EmailUtil.sendAccountCreated(
                    toEmail,
                    "testuser (TEST)",
                    "Staff",
                    "https://192.168.95.174:8443"
                    + request.getContextPath()
                    + "/login.jsp"
            );

            /* =====================
               TEST 4
               Password Changed
               ===================== */
            EmailUtil.sendPasswordChanged(
                    toEmail,
                    currentUser.getUsername()
                    + " (TEST)",
                    now,
                    request.getRemoteAddr()
            );

            /* =========================
               SUCCESS
               ========================= */
            response.sendRedirect(
                    request.getContextPath()
                    + "/dashboard?success="
                    + "Test+emails+sent+to+"
                    + toEmail.replace(
                            "@", "%40")
                    + "+Check+your+inbox!");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                    request.getContextPath()
                    + "/dashboard?error="
                    + "Email+test+failed:+"
                    + e.getMessage()
                            .replace(" ", "+"));
        }
    }
}
