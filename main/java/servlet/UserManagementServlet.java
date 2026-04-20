package servlet;

import java.io.IOException;
import java.util.List;

import dao.ActivityLogDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/user-management")
public class UserManagementServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO        userDAO = new UserDAO();
    private ActivityLogDAO logDAO  = new ActivityLogDAO();

    /* =========================
       GET — LIST ALL USERS
       ========================= */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User current = getUser(request, response);
        if (current == null) return;

        // ── Superadmin only ──
        if (!current.isSuperAdmin()) {
            response.sendRedirect(
                request.getContextPath() + "/dashboard");
            return;
        }

        String action = request.getParameter("action");

        // ── DELETE ──
        if ("delete".equals(action)) {
            handleDelete(request, response, current);
            return;
        }

        // ── TOGGLE ACTIVE ──
        if ("toggle".equals(action)) {
            handleToggle(request, response, current);
            return;
        }

        loadPage(request, response);
    }

    /* =========================
       POST — CREATE / UPDATE
       ========================= */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User current = getUser(request, response);
        if (current == null) return;

        if (!current.isSuperAdmin()) {
            response.sendRedirect(
                request.getContextPath() + "/dashboard");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "create" -> handleCreate(request, response, current);
            case "update" -> handleUpdate(request, response, current);
            default       -> response.sendRedirect(
                request.getContextPath() + "/user-management");
        }
    }

    /* =========================
       LOAD PAGE
       ========================= */
    private void loadPage(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        List<User> users = userDAO.getAllUsers();
        request.setAttribute("userList", users);
        request.setAttribute("successMsg",
            request.getParameter("success"));
        request.setAttribute("errorMsg",
            request.getParameter("error"));

        request.getRequestDispatcher("/user-management.jsp")
               .forward(request, response);
    }

    /* =========================
       CREATE USER
       ========================= */
    private void handleCreate(HttpServletRequest request,
                              HttpServletResponse response,
                              User current)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email    = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone    = request.getParameter("phone");
        String role     = request.getParameter("role");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role     == null || role.trim().isEmpty()) {

            response.sendRedirect(request.getContextPath()
                + "/user-management?error="
                + "Username,+password+and+role+are+required.");
            return;
        }

        // Check duplicate username
        if (userDAO.usernameExists(username.trim())) {
            response.sendRedirect(request.getContextPath()
                + "/user-management?error="
                + "Username+already+exists.");
            return;
        }

        // ── Hash password using SHA-256 ──
        String hashed = UserDAO.hashPassword(password.trim());

        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(hashed);
        u.setEmail(email != null ? email.trim() : null);
        u.setFullName(fullName != null ? fullName.trim() : null);
        u.setPhone(phone != null ? phone.trim() : null);
        u.setRole(role.trim());
        u.setActive(true);

        boolean saved = userDAO.createUser(u);

        if (saved) {
            logDAO.log(
                current.getUserId(),
                current.getUsername(),
                "ADD",
                "Users",
                "Created user: \""
                    + username.trim() + "\""
                    + " | Role: " + role.trim()
            );
            response.sendRedirect(request.getContextPath()
                + "/user-management?success="
                + "User+created+successfully.");
        } else {
            response.sendRedirect(request.getContextPath()
                + "/user-management?error="
                + "Failed+to+create+user.");
        }
    }

    /* =========================
       UPDATE USER
       ========================= */
    private void handleUpdate(HttpServletRequest request,
                              HttpServletResponse response,
                              User current)
            throws IOException {

        try {
            int    userId   = Integer.parseInt(
                                  request.getParameter("userId"));
            String fullName = request.getParameter("fullName");
            String email    = request.getParameter("email");
            String phone    = request.getParameter("phone");
            String role     = request.getParameter("role");
            boolean isActive = "1".equals(
                                  request.getParameter("isActive"));

            // Prevent superadmin from demoting themselves
            if (userId == current.getUserId() &&
                !"superadmin".equals(role)) {
                response.sendRedirect(request.getContextPath()
                    + "/user-management?error="
                    + "You+cannot+change+your+own+role.");
                return;
            }

            // Fetch old data for log
            User old = userDAO.getUserById(userId);
            String oldRole = (old != null) ? old.getRole() : "?";

            boolean updated = userDAO.adminUpdateUser(
                userId,
                fullName != null ? fullName.trim() : "",
                email    != null ? email.trim()    : "",
                phone    != null ? phone.trim()    : "",
                role.trim(),
                isActive
            );

            if (updated) {
                logDAO.log(
                    current.getUserId(),
                    current.getUsername(),
                    "UPDATE",
                    "Users",
                    "Updated user ID: " + userId
                        + " | Role: " + oldRole
                        + " → " + role.trim()
                        + " | Active: " + isActive
                );
                // ✅ Refresh session if current user was updated
                if (userId == current.getUserId()) {
                    User refreshed = userDAO.getUserById(userId);
                    request.getSession().setAttribute("currentUser", refreshed);
                }
                response.sendRedirect(request.getContextPath()
                    + "/user-management?success="
                    + "User+updated+successfully.");
            } else {
                response.sendRedirect(request.getContextPath()
                    + "/user-management?error="
                    + "Failed+to+update+user.");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                + "/user-management?error=Invalid+user+ID.");
        }
    }

    /* =========================
       DELETE USER
       ========================= */
    private void handleDelete(HttpServletRequest request,
                              HttpServletResponse response,
                              User current)
            throws IOException {

        try {
            int userId = Integer.parseInt(
                             request.getParameter("userId"));

            // Prevent self-delete
            if (userId == current.getUserId()) {
                response.sendRedirect(request.getContextPath()
                    + "/user-management?error="
                    + "You+cannot+delete+your+own+account.");
                return;
            }

            User target = userDAO.getUserById(userId);
            String targetName = (target != null)
                ? target.getUsername() : "Unknown";

            boolean deleted = userDAO.deleteUser(userId);

            if (deleted) {
                logDAO.log(
                    current.getUserId(),
                    current.getUsername(),
                    "DELETE",
                    "Users",
                    "Deleted user: \""
                        + targetName + "\""
                        + " (ID: " + userId + ")"
                );
                response.sendRedirect(request.getContextPath()
                    + "/user-management?success="
                    + "User+deleted+successfully.");
            } else {
                response.sendRedirect(request.getContextPath()
                    + "/user-management?error="
                    + "Failed+to+delete+user.");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                + "/user-management?error=Invalid+user+ID.");
        }
    }

    /* =========================
       TOGGLE ACTIVE
       ========================= */
    private void handleToggle(HttpServletRequest request,
                              HttpServletResponse response,
                              User current)
            throws IOException {

        try {
            int userId = Integer.parseInt(
                             request.getParameter("userId"));

            // Prevent self-deactivation
            if (userId == current.getUserId()) {
                response.sendRedirect(request.getContextPath()
                    + "/user-management?error="
                    + "You+cannot+deactivate+your+own+account.");
                return;
            }

            User target = userDAO.getUserById(userId);
            if (target == null) {
                response.sendRedirect(request.getContextPath()
                    + "/user-management?error=User+not+found.");
                return;
            }

            boolean newStatus = !target.isActive();

            userDAO.adminUpdateUser(
                userId,
                target.getFullName(),
                target.getEmail(),
                target.getPhone(),
                target.getRole(),
                newStatus
            );

            logDAO.log(
                current.getUserId(),
                current.getUsername(),
                "UPDATE",
                "Users",
                (newStatus ? "Activated" : "Deactivated")
                    + " user: \"" + target.getUsername() + "\""
            );

            response.sendRedirect(request.getContextPath()
                + "/user-management?success=User+status+updated.");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                + "/user-management?error=Invalid+user+ID.");
        }
    }

    /* =========================
       AUTH HELPERS
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

    /* =========================
       PASSWORD HASHING — SHA-256
       ========================= */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md =
                java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(
                password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }
}