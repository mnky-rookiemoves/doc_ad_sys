package servlet;

import java.io.IOException;

import dao.ActivityLogDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO        userDAO = new UserDAO();
    private ActivityLogDAO logDAO  = new ActivityLogDAO();

    /* =========================
       GET — SHOW PROFILE
       ========================= */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User current = getUser(request, response);
        if (current == null) return;

        // Reload fresh data from DB
        User fresh = userDAO.getUserById(current.getUserId());
        if (fresh == null) fresh = current;

        request.setAttribute("profileUser", fresh);
        request.setAttribute("successMsg",
            request.getParameter("success"));
        request.setAttribute("errorMsg",
            request.getParameter("error"));

        request.getRequestDispatcher("/profile.jsp")
               .forward(request, response);
    }

    /* =========================
       POST — UPDATE PROFILE
             OR CHANGE PASSWORD
       ========================= */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User current = getUser(request, response);
        if (current == null) return;

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "updateProfile"  ->
                handleUpdateProfile(request, response, current);
            case "changePassword" ->
                handleChangePassword(request, response, current);
            default -> response.sendRedirect(
                request.getContextPath() + "/profile");
        }
    }

    /* =========================
       UPDATE PROFILE INFO
       ========================= */
    private void handleUpdateProfile(HttpServletRequest request,
                                     HttpServletResponse response,
                                     User current)
            throws IOException {

        String fullName = request.getParameter("fullName");
        String email    = request.getParameter("email");
        String phone    = request.getParameter("phone");

        boolean updated = userDAO.updateProfile(
            current.getUserId(),
            fullName != null ? fullName.trim() : "",
            email    != null ? email.trim()    : "",
            phone    != null ? phone.trim()    : ""
        );

        if (updated) {
            // ── Refresh session with new data ──
            User refreshed =
                userDAO.getUserById(current.getUserId());
            if (refreshed != null) {
                request.getSession()
                       .setAttribute("currentUser", refreshed);
            }
            logDAO.log(
                current.getUserId(),
                current.getUsername(),
                "UPDATE",
                "Profile",
                "Updated own profile info"
            );
            response.sendRedirect(request.getContextPath()
                + "/profile?success=Profile+updated+successfully.");
        } else {
            response.sendRedirect(request.getContextPath()
                + "/profile?error=Failed+to+update+profile.");
        }
    }

    /* =========================
       CHANGE PASSWORD
       ========================= */
    private void handleChangePassword(HttpServletRequest request,
                                      HttpServletResponse response,
                                      User current)
            throws IOException {

        String oldPass  = request.getParameter("oldPassword");
        String newPass  = request.getParameter("newPassword");
        String confirm  = request.getParameter("confirmPassword");

        // ── Validate inputs ──
        if (oldPass == null || newPass == null || confirm == null ||
            oldPass.trim().isEmpty() || newPass.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath()
                + "/profile?error=All+password+fields+are+required.");
            return;
        }

        if (!newPass.equals(confirm)) {
            response.sendRedirect(request.getContextPath()
                + "/profile?error="
                + "New+passwords+do+not+match.");
            return;
        }

        if (newPass.length() < 6) {
            response.sendRedirect(request.getContextPath()
                + "/profile?error="
                + "Password+must+be+at+least+6+characters.");
            return;
        }

        // ── Verify old password ──
        String storedHash =
            userDAO.getPasswordById(current.getUserId());
        String oldHash =
            UserManagementServlet.hashPassword(oldPass.trim());

        if (!oldHash.equals(storedHash)) {
            response.sendRedirect(request.getContextPath()
                + "/profile?error=Current+password+is+incorrect.");
            return;
        }

        // ── Save new password ──
        String newHash =
            UserManagementServlet.hashPassword(newPass.trim());
        boolean updated =
            userDAO.updatePassword(current.getUserId(), newHash);

        if (updated) {
            logDAO.log(
                current.getUserId(),
                current.getUsername(),
                "UPDATE",
                "Profile",
                "Changed own password"
            );
            response.sendRedirect(request.getContextPath()
                + "/profile?success=Password+changed+successfully.");
        } else {
            response.sendRedirect(request.getContextPath()
                + "/profile?error=Failed+to+change+password.");
        }
    }

    /* =========================
       AUTH HELPER
       ========================= */
    private User getUser(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp");
            return null;
        }
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp");
        }
        return user;
    }
}